package com.bank.transaction.entities;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
public class UpdateBalanceRequest {

    @NotNull(message = "New balance amount is required")
    private BigDecimal newBalance;

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }

	public UpdateBalanceRequest(@NotNull(message = "New balance amount is required") BigDecimal newBalance) {
		super();
		this.newBalance = newBalance;
	}
    
}
