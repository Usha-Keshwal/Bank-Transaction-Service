package com.bank.transaction.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TransactionDTO {
	private Long id;
	private BigDecimal amount;
	private TransactionType type;
	private LocalDateTime timestamp;
	private BigDecimal balanceAfterTransaction;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public TransactionType getType() {
		return type;
	}
	public void setType(TransactionType type) {
		this.type = type;
	}
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	public BigDecimal getBalanceAfterTransaction() {
		return balanceAfterTransaction;
	}
	public void setBalanceAfterTransaction(BigDecimal balanceAfterTransaction) {
		this.balanceAfterTransaction = balanceAfterTransaction;
	}
	
}