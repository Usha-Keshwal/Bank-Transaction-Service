package com.bank.transaction.service.impl;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.bank.transaction.entities.AccountDTO;
import com.bank.transaction.entities.UpdateBalanceRequest;


@FeignClient(url = "http://localhost:8080",value = "Account-Client")
public interface AccountClient {
	
	@GetMapping("/api/accounts/{id}")
	AccountDTO getAccount(@PathVariable Long id);
	
	@PutMapping("/api/accounts/{id}/balance")
	AccountDTO updateBalance(@PathVariable Long id,@RequestBody UpdateBalanceRequest request);

}
