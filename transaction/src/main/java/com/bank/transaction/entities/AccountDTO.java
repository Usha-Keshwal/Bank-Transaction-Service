package com.bank.transaction.entities;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AccountDTO {
    private Long id;
    private String customerName;
    private BigDecimal balance;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
    
}