package com.endriw.financesync.controller;

import com.endriw.financesync.dto.AccountRequest;
import com.endriw.financesync.model.Account;
import com.endriw.financesync.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<Account>> findAll(Principal principal) {
        return ResponseEntity.ok(accountService.findAll(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> findById(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(accountService.findById(id, principal.getName()));
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody AccountRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.create(request, principal.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable Long id,
                                                 @Valid @RequestBody AccountRequest request,
                                                 Principal principal) {
        return ResponseEntity.ok(accountService.update(id, request, principal.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id, Principal principal) {
        accountService.delete(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

}
