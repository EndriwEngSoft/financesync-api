package com.endriw.financesync.controller;

import com.endriw.financesync.dto.TransactionRequest;
import com.endriw.financesync.model.Transaction;
import com.endriw.financesync.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> findAll(Principal principal) {
        return ResponseEntity.ok(transactionService.findAll(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> findById(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(transactionService.findById(id, principal.getName()));
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody TransactionRequest transaction, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.create(transaction, principal.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id,
                                                   @RequestBody TransactionRequest transaction,
                                                   Principal principal) {
        return ResponseEntity.ok(transactionService.update(id, transaction, principal.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>  deleteTransaction(@PathVariable Long id, Principal principal) {
        transactionService.delete(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

}
