package com.progressive.banking.moneytransfer.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.progressive.banking.moneytransfer.domain.dto.AccountResponse;
import com.progressive.banking.moneytransfer.domain.dto.BalanceResponse;
import com.progressive.banking.moneytransfer.domain.dto.TransferResponse;
import com.progressive.banking.moneytransfer.domain.entities.Account;
import com.progressive.banking.moneytransfer.domain.entities.TransactionLog;
import com.progressive.banking.moneytransfer.domain.enums.AccountStatusEnum;
import com.progressive.banking.moneytransfer.domain.enums.TransactionStatusEnum;
import com.progressive.banking.moneytransfer.exception.AccountNotFoundException;
import com.progressive.banking.moneytransfer.repository.AccountRepository;
import com.progressive.banking.moneytransfer.repository.TransactionLogRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionLogRepository transactionLogRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account account(Integer id, String name, BigDecimal balance) {
        Account a = new Account();
        a.setAccountId(id);
        a.setHolderName(name);
        a.setBalance(balance);
        a.setStatus(AccountStatusEnum.ACTIVE);
        a.setVersion(1L);
        a.setLastUpdated(LocalDateTime.now());
        return a;
    }

    @Test
    @DisplayName("getAccount returns AccountResponse when account exists")
    void getAccount_whenExists_returnsResponse() {
        Integer id = 1;
        Account account = account(id, "Alice", BigDecimal.valueOf(500));
        when(accountRepository.findById(eq(id))).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccount(id);

        assertEquals(id, response.getAccountId());
        assertEquals("Alice", response.getHolderName());
        assertEquals(BigDecimal.valueOf(500), response.getBalance());
        verify(accountRepository).findById(id);
    }

    @Test
    @DisplayName("getAccount throws AccountNotFoundException when account missing")
    void getAccount_whenMissing_throwsNotFound() {
        Integer id = 999;
        when(accountRepository.findById(eq(id))).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount(id));
        verify(accountRepository).findById(id);
    }

    @Test
    @DisplayName("getBalance returns BalanceResponse when account exists")
    void getBalance_whenExists_returnsResponse() {
        Integer id = 2;
        Account account = account(id, "Bob", BigDecimal.valueOf(250));
        when(accountRepository.findById(eq(id))).thenReturn(Optional.of(account));

        BalanceResponse response = accountService.getBalance(id);

        assertEquals(id, response.getAccountId());
        assertEquals(BigDecimal.valueOf(250), response.getBalance());
        verify(accountRepository).findById(id);
    }

    @Test
    @DisplayName("getBalance throws AccountNotFoundException when account missing")
    void getBalance_whenMissing_throwsNotFound() {
        Integer id = 999;
        when(accountRepository.findById(eq(id))).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getBalance(id));
    }

    @Test
    @DisplayName("getTransactions returns list when account exists")
    void getTransactions_whenExists_returnsList() {
        Integer id = 3;
        Account account = account(id, "Carol", BigDecimal.ZERO);
        TransactionLog log = new TransactionLog();
        log.setTransactionId(10);
        log.setFromAccountId(id);
        log.setToAccountId(4);
        log.setAmount(BigDecimal.valueOf(100));
        log.setStatus(TransactionStatusEnum.SUCCESS);
        log.setIdempotencyKey("key-1");
        log.setCreatedOn(LocalDateTime.now());

        when(accountRepository.findById(eq(id))).thenReturn(Optional.of(account));
        when(transactionLogRepository.findByFromAccountIdOrToAccountIdOrderByCreatedOnDesc(eq(id), eq(id)))
                .thenReturn(List.of(log));

        List<TransferResponse> result = accountService.getTransactions(id);

        assertEquals(1, result.size());
        assertEquals(id, result.get(0).getFromAccountId());
        assertEquals(4, result.get(0).getToAccountId());
        assertEquals(BigDecimal.valueOf(100), result.get(0).getAmount());
        verify(accountRepository).findById(id);
        verify(transactionLogRepository).findByFromAccountIdOrToAccountIdOrderByCreatedOnDesc(id, id);
    }

    @Test
    @DisplayName("getTransactions throws AccountNotFoundException when account missing")
    void getTransactions_whenMissing_throwsNotFound() {
        Integer id = 999;
        when(accountRepository.findById(eq(id))).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getTransactions(id));
    }
}
