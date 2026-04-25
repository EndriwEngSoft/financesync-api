package com.endriw.financesync.controller;

import com.endriw.financesync.dto.TransactionRequest;
import com.endriw.financesync.model.Account;
import com.endriw.financesync.model.Category;
import com.endriw.financesync.model.Transaction;
import com.endriw.financesync.model.User;
import com.endriw.financesync.model.enums.*;
import com.endriw.financesync.security.CustomUserDetailsService;
import com.endriw.financesync.security.JwtService;
import com.endriw.financesync.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    public static final String ACCOUNT_NUMBER = "1234";
    public static final String AGENCY = "0001";
    public static final String BANK_NAME = "Nubank";
    public static final AccountType ACCOUNT_TYPE = AccountType.CHECKING;
    public static final AccountStatus ACCOUNT_STATUS = AccountStatus.ACTIVE;
    public static final LocalDateTime CREATED_AT = LocalDateTime.now();

    public static final String CATEGORY_NAME = "Category 1";
    public static final String CATEGORY_DESCRIPTION = "Description 1";

    public static final BigDecimal AMOUNT = new BigDecimal("100.00");
    public static final TransactionType TRANSACTION_TYPE = TransactionType.EXPENSE;
    public static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.PENDING;
    public static final PaymentMethod PAYMENT_METHOD = PaymentMethod.CREDIT_CARD;
    public static final BigDecimal FEE = new BigDecimal("0.00");
    public static final String TRANSACTION_DESCRIPTION = "Gasto com alimentação";

    public static final String EMAIL = "test@email.com";


    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    TransactionService transactionService;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    CustomUserDetailsService userDetailsService;

    private User user;
    private Account account;
    private Category category;
    private Transaction transaction;

    private TransactionRequest buildValidRequest() {

        TransactionRequest request = new TransactionRequest();

        request.setAmount(AMOUNT);
        request.setType(TRANSACTION_TYPE);
        request.setPaymentMethod(PAYMENT_METHOD);
        request.setFee(FEE);
        request.setDescription(TRANSACTION_DESCRIPTION);
        request.setAccountId(account.getId());
        request.setCategoryId(category.getId());

        return request;
    }

    private TransactionRequest buildRequestWithNegativeAmount() {
        TransactionRequest request = buildValidRequest();
        request.setAmount(new BigDecimal("-100.00"));
        return request;
    }

    private ResultActions expectValidTransactionResponse(ResultActions resultActions, String jsonPathPrefix) throws Exception {
        return resultActions
                .andExpect(jsonPath(jsonPathPrefix + ".amount").value(100.0))
                .andExpect(jsonPath(jsonPathPrefix + ".type").value(TRANSACTION_TYPE.toString()))
                .andExpect(jsonPath(jsonPathPrefix + ".status").value(TRANSACTION_STATUS.toString()))
                .andExpect(jsonPath(jsonPathPrefix + ".paymentMethod").value(PAYMENT_METHOD.toString()))
                .andExpect(jsonPath(jsonPathPrefix + ".fee").value(0.0))
                .andExpect(jsonPath(jsonPathPrefix + ".description").value(TRANSACTION_DESCRIPTION))
                .andExpect(jsonPath(jsonPathPrefix + ".transactionDate").isNotEmpty())
                .andExpect(jsonPath(jsonPathPrefix + ".account.id").value(account.getId()))
                .andExpect(jsonPath(jsonPathPrefix + ".category.id").value(category.getId()));
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
        account.setStatus(ACCOUNT_STATUS);
        account.setCreatedAt(CREATED_AT);
        account.setUser(user);
        account.setId(1L);

        category = new Category();

        category.setName(CATEGORY_NAME);
        category.setDescription(CATEGORY_DESCRIPTION);
        category.setCreatedAt(CREATED_AT);
        category.setUser(user);
        category.setId(1L);

        transaction = new  Transaction();

        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setAmount(AMOUNT);
        transaction.setType(TRANSACTION_TYPE);
        transaction.setStatus(TRANSACTION_STATUS);
        transaction.setPaymentMethod(PAYMENT_METHOD);
        transaction.setFee(FEE);
        transaction.setDescription(TRANSACTION_DESCRIPTION);
        transaction.setTransactionDate(CREATED_AT);

    }

    @Test
    @WithMockUser(username = EMAIL)
    void findAll_whenTransactionExist_shouldReturnListAndOk() throws Exception {

        when((transactionService.findAll(anyString()))).thenReturn(List.of(transaction));

        expectValidTransactionResponse(
                mockMvc.perform(get("/transactions"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON)),
                "$[0]");
    }

    @Test
    @WithMockUser(username = EMAIL)
    void findAll_whenNoTransactionExist_shouldReturnEmptyListAndOk() throws Exception {

        when(transactionService.findAll(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = EMAIL)
    void findAll_whenUserNotFound_ShouldReturnNotFound() throws Exception {

        when(transactionService.findAll(anyString())).thenThrow(
                new RuntimeException("User not found: " + EMAIL));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found: " + EMAIL))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void findAll_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void findById_whenTransactionExists_shouldReturnTransactionAndOk() throws Exception {

        when(transactionService.findById(anyLong(), anyString())).thenReturn(transaction);

        expectValidTransactionResponse(
                mockMvc.perform(get("/transactions/{id}", 1L))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON)),
                "$");
    }

    @Test
    @WithMockUser(username = EMAIL)
    void findById_whenTransactionNotFound_ShouldReturnNotFound() throws Exception {

        when(transactionService.findById(anyLong(), anyString())).thenThrow(
                new RuntimeException("Transaction not found"));

        mockMvc.perform(get("/transactions/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Transaction not found"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void findById_whenTransactionBelongsToAnotherUser_shouldReturnForbidden() throws Exception {

        when(transactionService.findById(anyLong(), anyString())).thenThrow(
                new RuntimeException("Transaction does not belong to this user")
        );

        mockMvc.perform(get("/transactions/{id}", 1L))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Transaction does not belong to this user"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void findById_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {

        mockMvc.perform(get("/transactions/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void create_whenRequestIsValid_shouldReturnCreated() throws Exception {

        TransactionRequest request = buildValidRequest();

        when(transactionService.create(any(TransactionRequest.class), anyString())).thenReturn(transaction);

        expectValidTransactionResponse(
                mockMvc.perform(post("/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON)),
                "$");
    }

    @Test
    @WithMockUser(username = EMAIL)
    void create_whenRequestIsInvalid_shouldReturnBadRequest() throws Exception {

        TransactionRequest request = buildRequestWithNegativeAmount();

        mockMvc.perform(post("/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("amount: must be greater than 0"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void create_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {

        mockMvc.perform(post("/transactions")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void update_whenRequestIsValid_shouldReturnUpdated() throws Exception {

        TransactionRequest request = buildValidRequest();

        request.setAmount(new BigDecimal("100.00"));
        request.setDescription(TRANSACTION_DESCRIPTION);

        Transaction updateTransaction = new  Transaction();

        updateTransaction.setAccount(account);
        updateTransaction.setCategory(category);
        updateTransaction.setAmount(AMOUNT);
        updateTransaction.setType(TRANSACTION_TYPE);
        updateTransaction.setStatus(TRANSACTION_STATUS);
        updateTransaction.setPaymentMethod(PAYMENT_METHOD);
        updateTransaction.setFee(FEE);
        updateTransaction.setDescription(TRANSACTION_DESCRIPTION);
        updateTransaction.setTransactionDate(CREATED_AT);

        when(transactionService.update(anyLong(), any(TransactionRequest.class), anyString())).thenReturn(updateTransaction);

        expectValidTransactionResponse(
                mockMvc.perform(put("/transactions/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON)),
                "$");
    }

    @Test
    @WithMockUser(username = EMAIL)
    void update_whenRequestIsInvalid_shouldReturnBadRequest() throws Exception {

        TransactionRequest request = buildRequestWithNegativeAmount();

        mockMvc.perform(put("/transactions/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("amount: must be greater than 0"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void update_whenTransactionNotFound_shouldReturnNotFound() throws Exception {

        TransactionRequest request = buildValidRequest();

        when(transactionService.update(anyLong(), any(TransactionRequest.class), anyString())).thenThrow(
                new RuntimeException("Transaction not found")
        );

        mockMvc.perform(put("/transactions/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Transaction not found"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void update_whenTransactionBelongsToAnotherUser_shouldReturnForbidden() throws Exception {

        TransactionRequest request = buildValidRequest();

        when(transactionService.update(anyLong(), any(TransactionRequest.class), anyString())).thenThrow(
                new RuntimeException("Transaction does not belong to this user")
        );

        mockMvc.perform(put("/transactions/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Transaction does not belong to this user"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void update_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {

        mockMvc.perform((put("/transactions/{id}", 1L))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void delete_whenTransactionExists_shouldReturnNoContent() throws Exception {

        doNothing().when(transactionService).delete(anyLong(), anyString());

        mockMvc.perform(delete("/transactions/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void delete_whenTransactionNotFound_shouldReturnNotFound() throws Exception {

        doThrow(new RuntimeException("Transaction not found"))
                .when(transactionService).delete(anyLong(), anyString());

        mockMvc.perform(delete("/transactions/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Transaction not found"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void delete_whenTransactionBelongsToAnotherUser_shouldReturnForbidden() throws Exception {

        doThrow(new RuntimeException("Transaction does not belong to this user"))
                .when(transactionService).delete(anyLong(), anyString());

        mockMvc.perform(delete("/transactions/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Transaction does not belong to this user"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void delete_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {

        mockMvc.perform(delete("/transactions/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

}
