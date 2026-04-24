package com.endriw.financesync.controller;

import com.endriw.financesync.dto.AccountRequest;
import com.endriw.financesync.model.Account;
import com.endriw.financesync.model.User;
import com.endriw.financesync.model.enums.AccountStatus;
import com.endriw.financesync.model.enums.AccountType;
import com.endriw.financesync.security.CustomUserDetailsService;
import com.endriw.financesync.security.JwtService;
import com.endriw.financesync.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    public static final String ACCOUNT_NUMBER = "1234";
    public static final String AGENCY = "0001";
    public static final String BANK_NAME = "Nubank";
    public static final String EMAIL = "test@email.com";
    public static final AccountType ACCOUNT_TYPE = AccountType.CHECKING;
    public static final AccountStatus ACCOUNT_STATUS = AccountStatus.ACTIVE;
    public static final LocalDateTime CREATED_AT = LocalDateTime.now();

    public static final String NEW_ACCOUNT_NUMBER = "2345";
    public static final String NEW_AGENCY = "0002";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AccountService accountService;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    CustomUserDetailsService userDetailsService;

    private User user;
    private Account account;

    private AccountRequest buildValidRequest() {

        AccountRequest request = new AccountRequest();

        request.setAccountNumber(ACCOUNT_NUMBER);
        request.setAgency(AGENCY);
        request.setBankName(BANK_NAME);
        request.setType(ACCOUNT_TYPE);

        return request;
    }

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        account = new Account();

        account.setAccountNumber(ACCOUNT_NUMBER);
        account.setAgency(AGENCY);
        account.setBankName(BANK_NAME);
        account.setType(ACCOUNT_TYPE);
        account.setUser(user);
        account.setStatus(ACCOUNT_STATUS);
        account.setCreatedAt(CREATED_AT);

    }

    @Test
    @WithMockUser(username = EMAIL)
    void findAll_whenAccountsExist_ShouldReturnListAndOk() throws Exception {

        when(accountService.findAll(anyString())).thenReturn(List.of(account));

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].accountNumber").value(ACCOUNT_NUMBER))
                .andExpect(jsonPath("$[0].agency").value(AGENCY))
                .andExpect(jsonPath("$[0].bankName").value(BANK_NAME))
                .andExpect(jsonPath("$[0].type").value(ACCOUNT_TYPE.toString()))
                .andExpect(jsonPath("$[0].status").value(ACCOUNT_STATUS.toString()))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void findAll_whenNoAccountsExist_shouldReturnEmptyListAndOk() throws Exception {

        when(accountService.findAll(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = EMAIL)
    void findAll_whenUserNotFound_shouldReturnNotFound() throws Exception {

        when(accountService.findAll(anyString())).thenThrow(
                new RuntimeException("User not found: " + EMAIL));

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found: " + EMAIL))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void findAll_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void findById_whenAccountExists_shouldReturnAccountAndOk() throws Exception {

        when(accountService.findById(anyLong(), anyString())).thenReturn(account);

        mockMvc.perform(get("/accounts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountNumber").value(ACCOUNT_NUMBER))
                .andExpect(jsonPath("$.agency").value(AGENCY))
                .andExpect(jsonPath("$.bankName").value(BANK_NAME))
                .andExpect(jsonPath("$.type").value(ACCOUNT_TYPE.toString()))
                .andExpect(jsonPath("$.status").value(ACCOUNT_STATUS.toString()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void findById_whenAccountNotFound_shouldReturnNotFound() throws Exception {

        when(accountService.findById(anyLong(), anyString())).thenThrow(
                new RuntimeException("Account not found with id: 1"));

        mockMvc.perform(get("/accounts/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Account not found with id: " + 1L))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void findById_whenAccountBelongsToAnotherUser_shouldReturnForbidden() throws Exception {

        when(accountService.findById(anyLong(), anyString())).thenThrow(
                new RuntimeException("Account does not belong to this user: 1")
        );

        mockMvc.perform(get("/accounts/{id}", 1L))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Account does not belong to this user: 1"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void findById_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {

        mockMvc.perform(get("/accounts/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void createAccount_whenRequestIsValid_shouldReturnCreated() throws Exception {

        AccountRequest request = buildValidRequest();

        when(accountService.create(any(AccountRequest.class), anyString())).thenReturn(account);

        mockMvc.perform(post("/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountNumber").value(ACCOUNT_NUMBER))
                .andExpect(jsonPath("$.agency").value(AGENCY))
                .andExpect(jsonPath("$.bankName").value(BANK_NAME))
                .andExpect(jsonPath("$.type").value(ACCOUNT_TYPE.toString()))
                .andExpect(jsonPath("$.status").value(ACCOUNT_STATUS.toString()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void createAccount_whenRequestIsInvalid_shouldReturnBadRequest() throws Exception {

        AccountRequest request = new AccountRequest();

        request.setAccountNumber("");
        request.setAgency(NEW_AGENCY);
        request.setBankName(BANK_NAME);
        request.setType(ACCOUNT_TYPE);

        mockMvc.perform(post("/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("accountNumber: must not be blank"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void createAccount_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {

        mockMvc.perform(post("/accounts")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void updateAccount_whenRequestIsValid_shouldReturnUpdated() throws Exception {

        AccountRequest request = new AccountRequest();

        request.setAccountNumber(NEW_ACCOUNT_NUMBER);
        request.setAgency(NEW_AGENCY);
        request.setBankName(BANK_NAME);
        request.setType(ACCOUNT_TYPE);

        Account updatedAccount = new Account();

        updatedAccount.setAccountNumber(NEW_ACCOUNT_NUMBER);
        updatedAccount.setAgency(NEW_AGENCY);
        updatedAccount.setBankName(BANK_NAME);
        updatedAccount.setType(ACCOUNT_TYPE);
        updatedAccount.setStatus(ACCOUNT_STATUS);
        updatedAccount.setCreatedAt(CREATED_AT);

        when(accountService.update(anyLong(),any(AccountRequest.class),anyString())).thenReturn(updatedAccount);

        mockMvc.perform(put("/accounts/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountNumber").value(NEW_ACCOUNT_NUMBER))
                .andExpect(jsonPath("$.agency").value(NEW_AGENCY))
                .andExpect(jsonPath("$.bankName").value(BANK_NAME))
                .andExpect(jsonPath("$.type").value(ACCOUNT_TYPE.toString()))
                .andExpect(jsonPath("$.status").value(ACCOUNT_STATUS.toString()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void updateAccount_whenRequestIsInvalid_shouldReturnBadRequest() throws Exception {

        AccountRequest request = new AccountRequest();

        request.setAccountNumber("");
        request.setAgency(NEW_AGENCY);
        request.setBankName(BANK_NAME);
        request.setType(ACCOUNT_TYPE);

        mockMvc.perform(put("/accounts/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("accountNumber: must not be blank"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void updateAccount_whenAccountNotFound_shouldReturnNotFound() throws Exception {

        AccountRequest request = new AccountRequest();

        request.setAccountNumber(NEW_ACCOUNT_NUMBER);
        request.setAgency(NEW_AGENCY);
        request.setBankName(BANK_NAME);
        request.setType(ACCOUNT_TYPE);

        when(accountService.update(anyLong(),any(AccountRequest.class),anyString())).thenThrow(
                new RuntimeException("Account not found with id: 1")
        );

        mockMvc.perform(put("/accounts/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Account not found with id: " + 1L))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void updateAccount_whenAccountBelongsToAnotherUser_shouldReturnForbidden() throws Exception {

        AccountRequest request = new AccountRequest();

        request.setAccountNumber(NEW_ACCOUNT_NUMBER);
        request.setAgency(NEW_AGENCY);
        request.setBankName(BANK_NAME);
        request.setType(ACCOUNT_TYPE);

        when(accountService.update(anyLong(),any(AccountRequest.class),anyString())).thenThrow(
                new RuntimeException("Account does not belong to this user: 1")
        );

        mockMvc.perform(put("/accounts/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Account does not belong to this user: " + 1L))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void updateAccount_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {

        mockMvc.perform(put("/accounts/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void deleteAccount_whenAccountExists_shouldReturnNoContent() throws Exception {

        doNothing().when(accountService).delete(anyLong(), anyString());

        mockMvc.perform(delete("/accounts/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void deleteAccount_whenAccountNotFound_shouldReturnNotFound() throws Exception {

        doThrow(new RuntimeException("Account not found with id: 1")).
                when(accountService).delete(anyLong(), anyString());

        mockMvc.perform(delete("/accounts/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Account not found with id: " + 1L))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void deleteAccount_whenAccountBelongsToAnotherUser_shouldReturnForbidden() throws Exception {

        doThrow(new RuntimeException("Account does not belong to this user: 1")).
                when(accountService).delete(anyLong(), anyString());

        mockMvc.perform(delete("/accounts/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Account does not belong to this user: " + 1L))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void deleteAccount_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {

        mockMvc.perform(delete("/accounts/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}