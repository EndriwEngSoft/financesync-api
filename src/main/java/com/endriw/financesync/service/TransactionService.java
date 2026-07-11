package com.endriw.financesync.service;

import com.endriw.financesync.client.FrankfurterClient;
import com.endriw.financesync.dto.TransactionRequest;
import com.endriw.financesync.dto.integration.CurrencyConversionResponse;
import com.endriw.financesync.model.*;
import com.endriw.financesync.model.enums.TransactionStatus;
import com.endriw.financesync.repository.AccountRepository;
import com.endriw.financesync.repository.CategoryRepository;
import com.endriw.financesync.repository.TransactionRepository;
import com.endriw.financesync.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private final FrankfurterClient frankfurterClient;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              CategoryRepository categoryRepository,
                              UserRepository userRepository,
                              FrankfurterClient frankfurterClient) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.frankfurterClient = frankfurterClient;
    }

    public Transaction create(TransactionRequest request, String email) {
        User user = getAuthenticatedUser(email);
        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setFee(request.getFee());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(LocalDateTime.now());

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Account does not belong to this user");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Category does not belong to this user");
        }

        transaction.setAccount(account);
        transaction.setCategory(category);

        String accountCurrency = account.getCurrency();
        String transactionCurrency = request.getCurrency();

        if (!accountCurrency.equalsIgnoreCase(transactionCurrency)) {

            CurrencyConversionResponse quote = frankfurterClient.getCurrencyConversion(accountCurrency, transactionCurrency);

            CurrencyConversion currencyConversion = new CurrencyConversion();
            currencyConversion.setBase(accountCurrency);
            currencyConversion.setQuote(transactionCurrency);

            currencyConversion.setRate(quote.rate());
            currencyConversion.setDate(quote.date());

            currencyConversion.setTransaction(transaction);
            transaction.setCurrencyConversion(currencyConversion);

            BigDecimal convertedAmount = request.getAmount()
                    .multiply(quote.rate())
                    .setScale(4, RoundingMode.HALF_UP);
            transaction.setAmountConverted(convertedAmount);

        } else {
            transaction.setAmountConverted(request.getAmount());
        }

        return transactionRepository.save(transaction);
    }

    public List<Transaction> findAll(String email) {
        User user = getAuthenticatedUser(email);
        return transactionRepository.findByAccountUser(user);
    }

    public Transaction findById(Long id, String email) {
        User user = getAuthenticatedUser(email);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getAccount().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Transaction does not belong to this user");
        }

        return transaction;
    }

    public Transaction update(Long id, TransactionRequest request, String email) {
        Transaction updateTransaction = findById(id, email);
        updateTransaction.setAmount(request.getAmount());
        updateTransaction.setType(request.getType());
        updateTransaction.setPaymentMethod(request.getPaymentMethod());
        updateTransaction.setFee(request.getFee());
        updateTransaction.setDescription(request.getDescription());
        updateTransaction.setTransactionDate(LocalDateTime.now());
        return transactionRepository.save(updateTransaction);
    }

    public void delete(Long id, String email) {
        Transaction transaction = findById(id, email);
        transactionRepository.delete(transaction);
    }

    private User getAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}
