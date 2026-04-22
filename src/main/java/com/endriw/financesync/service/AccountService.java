package com.endriw.financesync.service;

import com.endriw.financesync.dto.AccountRequest;
import com.endriw.financesync.model.Account;
import com.endriw.financesync.model.User;
import com.endriw.financesync.model.enums.AccountStatus;
import com.endriw.financesync.repository.AccountRepository;
import com.endriw.financesync.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public Account create(AccountRequest request, String email) {
        User user = getAuthenticatedUser(email);
        Account account = new Account();
        account.setAccountNumber(request.getAccountNumber());
        account.setAgency(request.getAgency());
        account.setBankName(request.getBankName());
        account.setType(request.getType());
        account.setUser(user);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }

    public List<Account> findAll(String email) {
        User user = getAuthenticatedUser(email);
        return accountRepository.findByUser(user);
    }

    public Account findById(Long id, String email) {
        User user = getAuthenticatedUser(email);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Account does not belong to this user: " + id);
        }

        return account;
    }

    public Account update(Long id, AccountRequest request, String email) {
        Account updateAccount = findById(id, email);
        updateAccount.setAccountNumber(request.getAccountNumber());
        updateAccount.setAgency(request.getAgency());
        updateAccount.setBankName(request.getBankName());
        updateAccount.setType(request.getType());
        return accountRepository.save(updateAccount);
    }

    public void delete(Long id, String email) {
        Account account = findById(id, email);
        accountRepository.delete(account);
    }

    private User getAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}
