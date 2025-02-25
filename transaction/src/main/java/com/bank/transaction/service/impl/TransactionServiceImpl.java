package com.bank.transaction.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.bank.transaction.entities.AccountDTO;
import com.bank.transaction.entities.Transaction;
import com.bank.transaction.entities.TransactionDTO;
import com.bank.transaction.entities.TransactionType;
import com.bank.transaction.entities.UpdateBalanceRequest;
import com.bank.transaction.exceptions.InsufficientBalanceException;
import com.bank.transaction.repositories.TransactionRepository;
import com.bank.transaction.service.TransactionService;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	
	private AccountClient accountClient;

	@Transactional
	public TransactionDTO deposit(Long accountId, BigDecimal amount) {
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Deposit amount must be positive");
		}

		AccountDTO account = getAccount(accountId);
		BigDecimal newBalance = account.getBalance().add(amount);

		Transaction transaction = new Transaction();
		transaction.setAccountId(accountId);
		transaction.setAmount(amount);
		transaction.setType(TransactionType.DEPOSIT);
		transaction.setBalanceAfterTransaction(newBalance);

		updateAccountBalance(accountId, newBalance);
		return mapToDTO(transactionRepository.save(transaction));
	}

	@Transactional
	public TransactionDTO withdraw(Long accountId, BigDecimal amount) {
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Withdrawal amount must be positive");
		}

		AccountDTO account = getAccount(accountId);
		if (account.getBalance().compareTo(amount) < 0) {
			throw new InsufficientBalanceException("Insufficient balance for withdrawal. Current balance: " 
                    + account.getBalance() + ", Withdrawal amount: " + amount);
		}

		BigDecimal newBalance = account.getBalance().subtract(amount);

		Transaction transaction = new Transaction();
		transaction.setAccountId(accountId);
		transaction.setAmount(amount);
		transaction.setType(TransactionType.WITHDRAWAL);
		transaction.setBalanceAfterTransaction(newBalance);

		updateAccountBalance(accountId, newBalance);
		return mapToDTO(transactionRepository.save(transaction));
	}

	public List<TransactionDTO> getLastTenTransactions(Long accountId) {
		return transactionRepository.findTop10ByAccountIdOrderByTimestampDesc(accountId).stream().map(this::mapToDTO)
				.collect(Collectors.toList());
	}

	private AccountDTO getAccount(Long accountId) {
		 try {
	            return accountClient.getAccount(accountId);
	        } catch (FeignException.NotFound e) {
	            throw new com.bank.transaction.exceptions.AccountNotFoundException("Account not found with id: " + accountId);
	        }
		
	}

	private void updateAccountBalance(Long accountId, BigDecimal newBalance) {
		
		try {
            UpdateBalanceRequest request = new UpdateBalanceRequest(newBalance);
            accountClient.updateBalance(accountId, request);
        } catch (FeignException.NotFound e) {
            throw new com.bank.transaction.exceptions.AccountNotFoundException("Account not found with id: " + accountId);
        }
	}

	private TransactionDTO mapToDTO(Transaction transaction) {
		TransactionDTO dto = new TransactionDTO();
		dto.setId(transaction.getId());
		dto.setAmount(transaction.getAmount());
		dto.setType(transaction.getType());
		dto.setTimestamp(transaction.getTimestamp());
		dto.setBalanceAfterTransaction(transaction.getBalanceAfterTransaction());
		return dto;
	}

}
