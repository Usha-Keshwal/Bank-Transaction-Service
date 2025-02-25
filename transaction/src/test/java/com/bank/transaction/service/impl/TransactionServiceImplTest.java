package com.bank.transaction.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.bank.transaction.entities.AccountDTO;
import com.bank.transaction.entities.Transaction;
import com.bank.transaction.entities.TransactionDTO;
import com.bank.transaction.entities.TransactionType;
import com.bank.transaction.entities.UpdateBalanceRequest;
import com.bank.transaction.exceptions.AccountNotFoundException;
import com.bank.transaction.exceptions.InsufficientBalanceException;
import com.bank.transaction.repositories.TransactionRepository;

import feign.FeignException;

@SpringBootTest
@ActiveProfiles("test") // Optional: if you have a test profile in your application properties
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class TransactionServiceImplTest {


    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountClient accountClient;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private AccountDTO testAccount;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        // Setup test account
        testAccount = new AccountDTO();
        testAccount.setId(1L);
        testAccount.setCustomerName("Test Customer");
        testAccount.setBalance(new BigDecimal("1000.00"));

        // Setup test transaction
        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setAccountId(1L);
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setType(TransactionType.DEPOSIT);
        testTransaction.setTimestamp(LocalDateTime.now());
        testTransaction.setBalanceAfterTransaction(new BigDecimal("1100.00"));
    }

    @Test
    void testDeposit_Success() {
        // Arrange
        BigDecimal depositAmount = new BigDecimal("100.00");
        BigDecimal newBalance = testAccount.getBalance().add(depositAmount);
        
        when(accountClient.getAccount(1L)).thenReturn(testAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        
        // Act
        TransactionDTO result = transactionService.deposit(1L, depositAmount);
        
        // Assert
        assertNotNull(result);
        assertEquals(testTransaction.getId(), result.getId());
        assertEquals(testTransaction.getAmount(), result.getAmount());
        assertEquals(testTransaction.getType(), result.getType());
        assertEquals(testTransaction.getBalanceAfterTransaction(), result.getBalanceAfterTransaction());
        
        // Verify that updateBalance was called with correct parameters
        verify(accountClient).updateBalance(eq(1L), any(UpdateBalanceRequest.class));
    }
    
    @Test
    void testDeposit_WithNegativeAmount_ShouldThrowException() {
        // Arrange
        BigDecimal negativeAmount = new BigDecimal("-50.00");
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.deposit(1L, negativeAmount);
        });
        
        assertEquals("Deposit amount must be positive", exception.getMessage());
        verify(accountClient, never()).getAccount(anyLong());
        verify(accountClient, never()).updateBalance(anyLong(), any(UpdateBalanceRequest.class));
    }
    
    @Test
    void testDeposit_WithZeroAmount_ShouldThrowException() {
        // Arrange
        BigDecimal zeroAmount = BigDecimal.ZERO;
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.deposit(1L, zeroAmount);
        });
        
        assertEquals("Deposit amount must be positive", exception.getMessage());
        verify(accountClient, never()).getAccount(anyLong());
        verify(accountClient, never()).updateBalance(anyLong(), any(UpdateBalanceRequest.class));
    }
    
    @Test
    void testDeposit_AccountNotFound_ShouldThrowException() {
        // Arrange
        BigDecimal depositAmount = new BigDecimal("100.00");
        when(accountClient.getAccount(999L)).thenThrow(FeignException.NotFound.class);
        
        // Act & Assert
        Exception exception = assertThrows(AccountNotFoundException.class, () -> {
            transactionService.deposit(999L, depositAmount);
        });
        
        assertTrue(exception.getMessage().contains("Account not found"));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
    
    @Test
    void testWithdraw_Success() {
        // Arrange
        BigDecimal withdrawAmount = new BigDecimal("100.00");
        testTransaction.setType(TransactionType.WITHDRAWAL);
        testTransaction.setBalanceAfterTransaction(new BigDecimal("900.00"));
        
        when(accountClient.getAccount(1L)).thenReturn(testAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        
        // Act
        TransactionDTO result = transactionService.withdraw(1L, withdrawAmount);
        
        // Assert
        assertNotNull(result);
        assertEquals(testTransaction.getId(), result.getId());
        assertEquals(testTransaction.getAmount(), result.getAmount());
        assertEquals(TransactionType.WITHDRAWAL, result.getType());
        assertEquals(testTransaction.getBalanceAfterTransaction(), result.getBalanceAfterTransaction());
        
        // Verify that updateBalance was called with correct parameters
        verify(accountClient).updateBalance(eq(1L), any(UpdateBalanceRequest.class));
    }
    
    @Test
    void testWithdraw_InsufficientBalance_ShouldThrowException() {
        // Arrange
        BigDecimal excessiveAmount = new BigDecimal("2000.00");
        when(accountClient.getAccount(1L)).thenReturn(testAccount);
        
        // Act & Assert
        Exception exception = assertThrows(InsufficientBalanceException.class, () -> {
            transactionService.withdraw(1L, excessiveAmount);
        });
        
        assertTrue(exception.getMessage().contains("Insufficient balance for withdrawal"));
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountClient, never()).updateBalance(anyLong(), any(UpdateBalanceRequest.class));
    }
    
    @Test
    void testWithdraw_WithNegativeAmount_ShouldThrowException() {
        // Arrange
        BigDecimal negativeAmount = new BigDecimal("-50.00");
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.withdraw(1L, negativeAmount);
        });
        
        assertEquals("Withdrawal amount must be positive", exception.getMessage());
        verify(accountClient, never()).getAccount(anyLong());
        verify(accountClient, never()).updateBalance(anyLong(), any(UpdateBalanceRequest.class));
    }
    
    @Test
    void testWithdraw_WithZeroAmount_ShouldThrowException() {
        // Arrange
        BigDecimal zeroAmount = BigDecimal.ZERO;
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.withdraw(1L, zeroAmount);
        });
        
        assertEquals("Withdrawal amount must be positive", exception.getMessage());
        verify(accountClient, never()).getAccount(anyLong());
        verify(accountClient, never()).updateBalance(anyLong(), any(UpdateBalanceRequest.class));
    }
    
    @Test
    void testWithdraw_AccountNotFound_ShouldThrowException() {
        // Arrange
        BigDecimal withdrawAmount = new BigDecimal("100.00");
        when(accountClient.getAccount(999L)).thenThrow(FeignException.NotFound.class);
        
        // Act & Assert
        Exception exception = assertThrows(AccountNotFoundException.class, () -> {
            transactionService.withdraw(999L, withdrawAmount);
        });
        
        assertTrue(exception.getMessage().contains("Account not found"));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
    
    @Test
    void testGetLastTenTransactions_Success() {
        // Arrange
        Transaction transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setAccountId(1L);
        transaction1.setAmount(new BigDecimal("100.00"));
        transaction1.setType(TransactionType.DEPOSIT);
        transaction1.setTimestamp(LocalDateTime.now().minusDays(1));
        transaction1.setBalanceAfterTransaction(new BigDecimal("1100.00"));
        
        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setAccountId(1L);
        transaction2.setAmount(new BigDecimal("50.00"));
        transaction2.setType(TransactionType.WITHDRAWAL);
        transaction2.setTimestamp(LocalDateTime.now());
        transaction2.setBalanceAfterTransaction(new BigDecimal("1050.00"));
        
        List<Transaction> transactionList = Arrays.asList(transaction2, transaction1); // Newer first
        
        when(transactionRepository.findTop10ByAccountIdOrderByTimestampDesc(1L)).thenReturn(transactionList);
        
        // Act
        List<TransactionDTO> result = transactionService.getLastTenTransactions(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(transaction2.getId(), result.get(0).getId());
        assertEquals(transaction2.getType(), result.get(0).getType());
        assertEquals(transaction1.getId(), result.get(1).getId());
        assertEquals(transaction1.getType(), result.get(1).getType());
    }
    
    @Test
    void testGetLastTenTransactions_EmptyList() {
        // Arrange
        when(transactionRepository.findTop10ByAccountIdOrderByTimestampDesc(1L)).thenReturn(List.of());
        
        // Act
        List<TransactionDTO> result = transactionService.getLastTenTransactions(1L);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
