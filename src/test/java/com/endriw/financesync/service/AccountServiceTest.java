package com.endriw.financesync.service;

import com.endriw.financesync.dto.AccountRequest;
import com.endriw.financesync.model.Account;
import com.endriw.financesync.model.User;
import com.endriw.financesync.model.enums.AccountStatus;
import com.endriw.financesync.model.enums.AccountType;
import com.endriw.financesync.repository.AccountRepository;
import com.endriw.financesync.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    public static final String ACCOUNT_NUMBER = "1234";
    public static final String AGENCY = "0001";
    public static final String BANK_NAME = "Nubank";
    public static final String EMAIL = "test@email.com";

    @Mock
    AccountRepository accountRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    AccountService accountService;

    @Test
    void create_whenUserExists_shouldSaveAccount() {
        User user = new User();
        AccountRequest request = new AccountRequest();

        Account account = new Account();

        account.setAccountNumber(ACCOUNT_NUMBER);
        account.setAgency(AGENCY);
        account.setBankName(BANK_NAME);
        account.setType(AccountType.CHECKING);
        account.setUser(user);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(accountRepository.save(any())).thenReturn(account);

        Account response = accountService.create(request, EMAIL);

        assertNotNull(response);
        verify(accountRepository, times(1)).save(any());
        assertEquals(ACCOUNT_NUMBER, response.getAccountNumber());
        assertEquals(AGENCY, response.getAgency());
        assertEquals(BANK_NAME, response.getBankName());
        assertEquals(AccountType.CHECKING, response.getType());
        assertEquals(user, response.getUser());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
        assertEquals(LocalDateTime.now().getDayOfYear(), response.getCreatedAt().getDayOfYear());
    }

    @Test
    void create_whenUserNotFound_shouldThrowException() {
        AccountRequest request = new AccountRequest();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.create(request, EMAIL));
        assertEquals("User not found: " + EMAIL, ex.getMessage());
    }

    @Test
    void findAll_whenUserExists_shouldReturnAccounts() {
        User user = new User();

        Account account = new Account();

        account.setAccountNumber(ACCOUNT_NUMBER);
        account.setAgency(AGENCY);
        account.setBankName(BANK_NAME);
        account.setType(AccountType.CHECKING);
        account.setUser(user);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());

        when(accountRepository.findByUser(user)).thenReturn(List.of(account));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        List<Account> response = accountService.findAll(EMAIL);

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
        verify(accountRepository, times(1)).findByUser(any());
        assertEquals(ACCOUNT_NUMBER, response.get(0).getAccountNumber());
        assertEquals(AGENCY, response.get(0).getAgency());
        assertEquals(BANK_NAME, response.get(0).getBankName());
        assertEquals(AccountType.CHECKING, response.get(0).getType());
        assertEquals(user, response.get(0).getUser());
        assertEquals(AccountStatus.ACTIVE, response.get(0).getStatus());
        assertEquals(LocalDateTime.now().getDayOfYear(), response.get(0).getCreatedAt().getDayOfYear());
    }

    @Test
    void findAll_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.findAll(EMAIL));
        assertEquals("User not found: " + EMAIL, ex.getMessage());
    }

    @Test
    void findById_whenAccountExists_shouldReturnAccount() {
        User user =  new User();
        user.setId(1L);
        Account account = new Account();

        account.setAccountNumber(ACCOUNT_NUMBER);
        account.setAgency(AGENCY);
        account.setBankName(BANK_NAME);
        account.setType(AccountType.CHECKING);
        account.setUser(user);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());

        when(accountRepository.findById(any())).thenReturn(Optional.of(account));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        Account response = accountService.findById(1L, EMAIL);

        assertNotNull(response);
        verify(accountRepository, times(1)).findById(any());
        assertEquals(ACCOUNT_NUMBER, response.getAccountNumber());
        assertEquals(AGENCY, response.getAgency());
        assertEquals(BANK_NAME, response.getBankName());
        assertEquals(AccountType.CHECKING, response.getType());
        assertEquals(user, response.getUser());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
        assertEquals(LocalDateTime.now().getDayOfYear(), response.getCreatedAt().getDayOfYear());
    }

    @Test
    void findById_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.findById(1L, EMAIL));
        assertEquals("User not found: " + EMAIL, ex.getMessage());
    }

    @Test
    void findById_whenAccountNotFound_shouldThrowException() {
        User user =  new User();
        user.setId(1L);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(accountRepository.findById(any())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.findById(1L, EMAIL));
        assertEquals("Account not found with id: 1", ex.getMessage());
    }

    @Test
    void findById_whenAccountBelongsToAnotherUser_shouldThrowException() {
        User user = new User();
        user.setId(1L);

        User user2 = new User();
        user2.setId(2L);

        Account account = new Account();
        account.setUser(user2);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(accountRepository.findById(any())).thenReturn(Optional.of(account));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.findById(1L, EMAIL));
        assertEquals("Account does not belong to this user: 1", ex.getMessage());
    }

    @Test
    void update_whenUserExists_shouldUpdateAccount() {
        AccountRequest request = new AccountRequest();

        request.setAccountNumber(ACCOUNT_NUMBER);
        request.setAgency(AGENCY);
        request.setBankName(BANK_NAME);
        request.setType(AccountType.CHECKING);

        User user = new User();
        user.setId(1L);

        Account account = new Account();

        account.setAccountNumber(ACCOUNT_NUMBER);
        account.setAgency(AGENCY);
        account.setBankName(BANK_NAME);
        account.setType(AccountType.CHECKING);
        account.setUser(user);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());

        when(accountRepository.findById(any())).thenReturn(Optional.of(account));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(accountRepository.save(any())).thenReturn(account);

        Account response = accountService.update(1L, request, EMAIL);

        assertNotNull(response);
        verify(accountRepository, times(1)).findById(any());
        assertEquals(ACCOUNT_NUMBER, response.getAccountNumber());
        assertEquals(AGENCY, response.getAgency());
        assertEquals(BANK_NAME, response.getBankName());
        assertEquals(AccountType.CHECKING, response.getType());
        assertEquals(user, response.getUser());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
        assertEquals(LocalDateTime.now().getDayOfYear(), response.getCreatedAt().getDayOfYear());
    }

    @Test
    void update_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.update(1L, new AccountRequest(), EMAIL));
        assertEquals("User not found: " + EMAIL, ex.getMessage());
    }

    @Test
    void delete_whenUserExists_shouldDeleteAccount() {
        User user = new User();
        user.setId(1L);

        Account account = new Account();

        account.setUser(user);
        account.setAccountNumber(ACCOUNT_NUMBER);
        account.setAgency(AGENCY);
        account.setBankName(BANK_NAME);
        account.setType(AccountType.CHECKING);

        when(accountRepository.findById(any())).thenReturn(Optional.of(account));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        accountService.delete(1L, EMAIL);

        verify(accountRepository, times(1)).findById(any());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(accountRepository, times(1)).delete(any());
    }

    @Test
    void delete_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.delete(1L, EMAIL));
        assertEquals("User not found: " + EMAIL, ex.getMessage());
    }
}