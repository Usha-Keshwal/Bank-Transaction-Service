package com.bank.transaction.repositories;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.transaction.entities.Transaction;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findTop10ByAccountIdOrderByTimestampDesc(Long accountId);
}