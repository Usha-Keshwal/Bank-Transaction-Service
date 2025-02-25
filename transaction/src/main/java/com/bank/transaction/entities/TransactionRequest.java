package com.bank.transaction.entities;
import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class TransactionRequest {
	@NotNull(message = "Amount is required")
	private BigDecimal amount;

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
}