package com.bank.transaction.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.transaction.entities.TransactionDTO;
import com.bank.transaction.entities.TransactionRequest;
import com.bank.transaction.service.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
	@Autowired
	private TransactionService transactionService;

    @PostMapping("/accounts/{id}/deposit")
    public ResponseEntity<TransactionDTO> deposit(
            @PathVariable Long id, 
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.deposit(id, request.getAmount()));
    }

    @PostMapping("/accounts/{id}/withdraw")
    public ResponseEntity<TransactionDTO> withdraw(
            @PathVariable Long id, 
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.withdraw(id, request.getAmount()));
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<List<TransactionDTO>> getTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getLastTenTransactions(id));
    }

}
