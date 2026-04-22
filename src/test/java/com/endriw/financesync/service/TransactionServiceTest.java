package com.endriw.financesync.service;

import com.endriw.financesync.dto.TransactionRequest;
import com.endriw.financesync.model.Account;
import com.endriw.financesync.model.Category;
import com.endriw.financesync.model.Transaction;
import com.endriw.financesync.model.User;
import com.endriw.financesync.model.enums.PaymentMethod;
import com.endriw.financesync.model.enums.TransactionStatus;
import com.endriw.financesync.model.enums.TransactionType;
import com.endriw.financesync.repository.AccountRepository;
import com.endriw.financesync.repository.CategoryRepository;
import com.endriw.financesync.repository.TransactionRepository;
import com.endriw.financesync.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    public static final String EMAIL = "test@email.com";
    public static final BigDecimal AMOUNT = new BigDecimal("100.00");
    public static final TransactionType TYPE = TransactionType.EXPENSE;
    public static final TransactionStatus STATUS = TransactionStatus.PENDING;
    public static final PaymentMethod PAYMENT_METHOD = PaymentMethod.CREDIT_CARD;
    public static final BigDecimal FEE = new BigDecimal("0.00");
    public static final String DESCRIPTION = "Gasto com alimentação";
    @Mock
    TransactionRepository transactionRepository;
    @Mock
    AccountRepository accountRepository;
    @Mock
    CategoryRepository categoryRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    TransactionService transactionService;

    @Test
    void create_whenUserExists_shouldCreateTransaction() {
        User user = new User();
        user.setId(1L);

        Category category = new Category();
        category.setUser(user);

        Account account = new Account();
        account.setUser(user);

        TransactionRequest request = new TransactionRequest();

        Transaction transaction = new Transaction();

        transaction.setAmount(AMOUNT);
        transaction.setType(TYPE);
        transaction.setStatus(STATUS);
        transaction.setPaymentMethod(PAYMENT_METHOD);
        transaction.setFee(FEE);
        transaction.setDescription(DESCRIPTION);
        transaction.setTransactionDate(LocalDateTime.now());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(categoryRepository.findById(any())).thenReturn(Optional.of(category));
        when(accountRepository.findById(any())).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction response = transactionService.create(request, EMAIL);

        verify(transactionRepository, times(1)).save(any(Transaction.class));

        assertNotNull(response);
        assertEquals(AMOUNT, response.getAmount());
        assertEquals(TYPE, response.getType());
        assertEquals(STATUS, response.getStatus());
        assertEquals(PAYMENT_METHOD, response.getPaymentMethod());
        assertEquals(FEE, response.getFee());
        assertEquals(DESCRIPTION, response.getDescription());
        assertEquals(LocalDateTime.now().getDayOfYear(), response.getTransactionDate().getDayOfYear());
    }

    @Test
    void create_whenUserNotFound_shouldThrowException() {
        TransactionRequest request = new TransactionRequest();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> transactionService.create(request, EMAIL));
        assertEquals("User not found: " + EMAIL, ex.getMessage());
    }

    @Test
    void create_whenAccountNotFound_shouldThrowException() {
        User user = new User();
        user.setId(1L);

        TransactionRequest request = new TransactionRequest();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(accountRepository.findById(any())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> transactionService.create(request, EMAIL));
        assertEquals("Account not found", ex.getMessage());
    }

    @Test
    void create_whenAccountBelongsToAnotherUser_shouldThrowException() {
        User user = new User();
        user.setId(1L);

        User user2 = new User();
        user2.setId(2L);

        Account account = new Account();
        account.setUser(user2);

        TransactionRequest request = new TransactionRequest();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(accountRepository.findById(any())).thenReturn(Optional.of(account));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> transactionService.create(request, EMAIL));
        assertEquals("Account does not belong to this user", ex.getMessage());
    }

    @Test
    void create_whenCategoryNotFound_shouldThrowException() {
        User user = new User();
        user.setId(1L);

        Account account = new Account();
        account.setUser(user);

        TransactionRequest request = new TransactionRequest();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(accountRepository.findById(any())).thenReturn(Optional.of(account));
        when(categoryRepository.findById(any())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> transactionService.create(request, EMAIL));
        assertEquals("Category not found", ex.getMessage());
    }

    @Test
    void create_whenCategoryBelongsToAnotherUser_shouldThrowException() {
        User user = new User();
        user.setId(1L);

        User user2 = new User();
        user2.setId(2L);

        Account account = new Account();
        account.setUser(user);

        Category category = new Category();
        category.setUser(user2);

        TransactionRequest request = new TransactionRequest();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(accountRepository.findById(any())).thenReturn(Optional.of(account));
        when(categoryRepository.findById(any())).thenReturn(Optional.of(category));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> transactionService.create(request, EMAIL));
        assertEquals("Category does not belong to this user", ex.getMessage());
    }

    @Test
    void findAll_whenUserExists_shouldReturnTransactions() {
        User user = new User();
        user.setId(1L);

        Transaction transaction = new Transaction();

        transaction.setAmount(AMOUNT);
        transaction.setType(TYPE);
        transaction.setStatus(STATUS);
        transaction.setPaymentMethod(PAYMENT_METHOD);
        transaction.setFee(FEE);
        transaction.setDescription(DESCRIPTION);
        transaction.setTransactionDate(LocalDateTime.now());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.findByAccountUser(user)).thenReturn(List.of(transaction));

        List<Transaction> response = transactionService.findAll(EMAIL);

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(1, response.size());

        assertEquals(AMOUNT, response.get(0).getAmount());
        assertEquals(TYPE, response.get(0).getType());
        assertEquals(STATUS, response.get(0).getStatus());
        assertEquals(PAYMENT_METHOD, response.get(0).getPaymentMethod());
        assertEquals(FEE, response.get(0).getFee());
        assertEquals(DESCRIPTION, response.get(0).getDescription());
        assertEquals(LocalDateTime.now().getDayOfYear(), response.get(0).getTransactionDate().getDayOfYear());
    }

    @Test
    void findAll_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> transactionService.findAll(EMAIL));
        assertEquals("User not found: " + EMAIL, ex.getMessage());
    }

    @Test
    void findById_whenTransactionExists_shouldReturnTransaction() {
        User user = new User();
        user.setId(1L);

        Account account = new Account();
        account.setUser(user);

        Transaction transaction = new Transaction();

        transaction.setAccount(account);
        transaction.setAmount(AMOUNT);
        transaction.setType(TYPE);
        transaction.setStatus(STATUS);
        transaction.setPaymentMethod(PAYMENT_METHOD);
        transaction.setFee(FEE);
        transaction.setDescription(DESCRIPTION);
        transaction.setTransactionDate(LocalDateTime.now());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.findById(any())).thenReturn(Optional.of(transaction));

        Transaction response = transactionService.findById(1L, EMAIL);

        assertNotNull(response);
        assertEquals(AMOUNT, response.getAmount());
        assertEquals(TYPE, response.getType());
        assertEquals(STATUS, response.getStatus());
        assertEquals(PAYMENT_METHOD, response.getPaymentMethod());
        assertEquals(FEE, response.getFee());
        assertEquals(DESCRIPTION, response.getDescription());
        assertEquals(LocalDateTime.now().getDayOfYear(), response.getTransactionDate().getDayOfYear());
        verify(userRepository, times(1)).findByEmail(anyString());
    }

    @Test
    void findById_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> transactionService.findById(1L, EMAIL));
        assertEquals("User not found: " + EMAIL, ex.getMessage());
    }

    @Test
    void findById_whenTransactionBelongsToAnotherUser_shouldThrowException() {
        User user = new User();
        user.setId(1L);

        User user2 = new User();
        user2.setId(2L);

        Account account = new Account();
        account.setUser(user2);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.findById(any())).thenReturn(Optional.of(transaction));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> transactionService.findById(1L, EMAIL));
        assertEquals("Transaction does not belong to this user", ex.getMessage());
    }

    @Test
    void update_whenUserExists_shouldUpdateTransaction() {
        User user = new User();
        user.setId(1L);

        Account account = new Account();
        account.setUser(user);

        TransactionRequest request = new TransactionRequest();

        request.setAmount(AMOUNT);
        request.setType(TYPE);
        request.setPaymentMethod(PAYMENT_METHOD);
        request.setFee(FEE);
        request.setDescription(DESCRIPTION);

        Transaction transaction = new Transaction();

        transaction.setAccount(account);
        transaction.setAmount(AMOUNT);
        transaction.setType(TYPE);
        transaction.setStatus(STATUS);
        transaction.setPaymentMethod(PAYMENT_METHOD);
        transaction.setFee(FEE);
        transaction.setDescription(DESCRIPTION);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.findById(any())).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction response = transactionService.update(1L, request, EMAIL);

        assertNotNull(response);
        assertEquals(AMOUNT, response.getAmount());
        assertEquals(TYPE, response.getType());
        assertEquals(PAYMENT_METHOD, response.getPaymentMethod());
        assertEquals(FEE, response.getFee());
        assertEquals(DESCRIPTION, response.getDescription());
        assertEquals(LocalDateTime.now().getDayOfYear(), response.getTransactionDate().getDayOfYear());
        verify(userRepository, times(1)).findByEmail(anyString());
    }

    @Test
    void update_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> transactionService.update(1L, new TransactionRequest(), EMAIL));
        assertEquals("User not found: " + EMAIL, ex.getMessage());
    }

    @Test
    void delete_whenUserExists_shouldDeleteTransaction() {
        User user = new User();
        user.setId(1L);

        Account account = new Account();
        account.setUser(user);

        Transaction transaction = new Transaction();

        transaction.setAccount(account);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(transactionRepository.findById(any())).thenReturn(Optional.of(transaction));

        transactionService.delete(1L, EMAIL);

        verify(userRepository, times(1)).findByEmail(anyString());
        verify(transactionRepository, times(1)).delete(any(Transaction.class));
    }

    @Test
    void delete_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> transactionService.delete(1L, EMAIL));
        assertEquals("User not found: " + EMAIL, ex.getMessage());
    }
}
