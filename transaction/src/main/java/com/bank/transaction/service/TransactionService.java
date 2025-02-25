package com.bank.transaction.service;

import java.math.BigDecimal;
import java.util.List;

import com.bank.transaction.entities.TransactionDTO;


public interface TransactionService {
	TransactionDTO deposit(Long accountId, BigDecimal amount);
	TransactionDTO withdraw(Long accountId, BigDecimal amount);
	List<TransactionDTO> getLastTenTransactions(Long accountId);

}
