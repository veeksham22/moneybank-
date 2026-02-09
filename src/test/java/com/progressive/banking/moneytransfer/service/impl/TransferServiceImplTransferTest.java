package com.progressive.banking.moneytransfer.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.progressive.banking.moneytransfer.domain.dto.TransferRequest;
import com.progressive.banking.moneytransfer.domain.dto.TransferResponse;
import com.progressive.banking.moneytransfer.domain.entities.Account;
import com.progressive.banking.moneytransfer.domain.entities.TransactionLog;
import com.progressive.banking.moneytransfer.domain.enums.AccountStatusEnum;
import com.progressive.banking.moneytransfer.domain.enums.TransactionStatusEnum;
import com.progressive.banking.moneytransfer.exception.AccountNotFoundException;
import com.progressive.banking.moneytransfer.exception.DuplicateTransferException;
import com.progressive.banking.moneytransfer.repository.AccountRepository;
import com.progressive.banking.moneytransfer.repository.TransactionLogRepository;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTransferTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionLogRepository transactionLogRepository;

    @InjectMocks
    private TransferServiceImpl transferService;

    private Account activeAccount(int id, BigDecimal balance) {
        Account a = new Account();
        a.setAccountId(id);
        a.setHolderName("User " + id);
        a.setBalance(balance);
        a.setStatus(AccountStatusEnum.ACTIVE);
        a.setVersion(1L);
        a.setLastUpdated(LocalDateTime.now());
        return a;
    }

    private TransferRequest request(int from, int to, BigDecimal amount, String idemKey) {
        TransferRequest r = new TransferRequest();
        r.setFromAccountId(from);
        r.setToAccountId(to);
        r.setAmount(amount);
        r.setIdempotencyKey(idemKey);
        return r;
    }

    @Test
    @DisplayName("transfer throws DuplicateTransferException when idempotency key already used")
    void transfer_duplicateIdempotencyKey_throwsDuplicateTransferException() {
        TransferRequest req = request(1, 2, BigDecimal.valueOf(100), "key-1");
        TransactionLog existing = new TransactionLog();
        existing.setTransactionId(99);
        existing.setIdempotencyKey("key-1");

        when(transactionLogRepository.findByIdempotencyKey(eq("key-1"))).thenReturn(Optional.of(existing));

        assertThrows(DuplicateTransferException.class, () -> transferService.transfer(req));
        verify(transactionLogRepository).findByIdempotencyKey("key-1");
        verify(accountRepository, never()).findById(any());
    }

    @Test
    @DisplayName("transfer throws AccountNotFoundException when from account missing")
    void transfer_fromAccountMissing_throwsNotFound() {
        TransferRequest req = request(1, 2, BigDecimal.valueOf(100), "key-2");
        when(transactionLogRepository.findByIdempotencyKey(eq("key-2"))).thenReturn(Optional.empty());
        when(accountRepository.findById(eq(1))).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> transferService.transfer(req));
        verify(accountRepository).findById(1);
        verify(accountRepository, never()).findById(2);
    }

    @Test
    @DisplayName("transfer throws AccountNotFoundException when to account missing")
    void transfer_toAccountMissing_throwsNotFound() {
        TransferRequest req = request(1, 2, BigDecimal.valueOf(100), "key-3");
        Account from = activeAccount(1, BigDecimal.valueOf(500));
        when(transactionLogRepository.findByIdempotencyKey(eq("key-3"))).thenReturn(Optional.empty());
        when(accountRepository.findById(eq(1))).thenReturn(Optional.of(from));
        when(accountRepository.findById(eq(2))).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> transferService.transfer(req));
        verify(accountRepository).findById(1);
        verify(accountRepository).findById(2);
    }

    @Test
    @DisplayName("transfer succeeds and returns TransferResponse with SUCCESS status")
    void transfer_validRequest_returnsSuccessResponse() {
        TransferRequest req = request(1, 2, BigDecimal.valueOf(100), "key-4");
        Account from = activeAccount(1, BigDecimal.valueOf(500));
        Account to = activeAccount(2, BigDecimal.valueOf(200));

        when(transactionLogRepository.findByIdempotencyKey(eq("key-4"))).thenReturn(Optional.empty());
        when(accountRepository.findById(eq(1))).thenReturn(Optional.of(from));
        when(accountRepository.findById(eq(2))).thenReturn(Optional.of(to));

        ArgumentCaptor<TransactionLog> logCaptor = ArgumentCaptor.forClass(TransactionLog.class);
        when(transactionLogRepository.save(logCaptor.capture())).thenAnswer(inv -> {
            TransactionLog log = inv.getArgument(0);
            if (log.getTransactionId() == null) {
                log.setTransactionId(42);
                log.setCreatedOn(LocalDateTime.now());
            }
            return log;
        });

        TransferResponse response = transferService.transfer(req);

        assertEquals(42, response.getTransactionId());
        assertEquals(1, response.getFromAccountId());
        assertEquals(2, response.getToAccountId());
        assertEquals(BigDecimal.valueOf(100), response.getAmount());
        assertEquals(TransactionStatusEnum.SUCCESS, response.getStatus());
        assertEquals("key-4", response.getIdempotencyKey());
        verify(accountRepository).save(from);
        verify(accountRepository).save(to);
        assertEquals(BigDecimal.valueOf(400), from.getBalance());
        assertEquals(BigDecimal.valueOf(300), to.getBalance());
    }
}
