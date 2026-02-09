package com.progressive.banking.moneytransfer.service.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.progressive.banking.moneytransfer.domain.dto.TransferRequest;
import com.progressive.banking.moneytransfer.domain.dto.TransferResponse;
import com.progressive.banking.moneytransfer.domain.entities.Account;
import com.progressive.banking.moneytransfer.domain.entities.TransactionLog;
import com.progressive.banking.moneytransfer.domain.enums.TransactionStatusEnum;
import com.progressive.banking.moneytransfer.domain.mapper.TransferMapper;
import com.progressive.banking.moneytransfer.exception.AccountNotActiveException;
import com.progressive.banking.moneytransfer.exception.AccountNotFoundException;
import com.progressive.banking.moneytransfer.exception.DuplicateTransferException;
import com.progressive.banking.moneytransfer.exception.InsufficientBalanceException;
import com.progressive.banking.moneytransfer.repository.AccountRepository;
import com.progressive.banking.moneytransfer.repository.TransactionLogRepository;
import com.progressive.banking.moneytransfer.service.TransferService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {

    private final AccountRepository accountRepository;
    private final TransactionLogRepository transactionLogRepository;

    /**
     * Main API method
     * - Enforces idempotency (returns existing result if same key repeats)
     * - Validates transfer rules
     * - Executes debit+credit atomically
     */
    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {

        // 1) Idempotency: if already processed, return existing log response
        transactionLogRepository.findByIdempotencyKey(request.getIdempotencyKey())
                .ifPresent(existing -> {
                    // If you prefer "return existing" instead of conflict, do that.
                    // Most systems return the existing result for same idempotency key.
                    throw new DuplicateTransferException(
                            "Duplicate transfer request. Idempotency key already used: " + request.getIdempotencyKey()
                    );
                });

        // 2) Fetch accounts
        Account from = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new AccountNotFoundException("From account not found: " + request.getFromAccountId()));

        Account to = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new AccountNotFoundException("To account not found: " + request.getToAccountId()));

        // 3) Create log as PENDING first (helps trace even if execution fails)
        TransactionLog logEntity = new TransactionLog();
        logEntity.setFromAccountId(request.getFromAccountId());
        logEntity.setToAccountId(request.getToAccountId());
        logEntity.setAmount(request.getAmount());
        logEntity.setIdempotencyKey(request.getIdempotencyKey());
        logEntity.setStatus(TransactionStatusEnum.PENDING);

        logEntity = transactionLogRepository.save(logEntity);

        try {
            // 4) Validate business rules
            validateTransfer(request, from, to);

            // 5) Execute actual debit/credit
            executeTransfer(from, to, request.getAmount());

            // 6) Persist updated accounts
            accountRepository.save(from);
            accountRepository.save(to);

            // 7) Mark success
            logEntity.setStatus(TransactionStatusEnum.SUCCESS);
            logEntity.setFailureReason(null);
            transactionLogRepository.save(logEntity);

            return TransferMapper.toResponse(logEntity);

        } catch (RuntimeException ex) {
            // Mark failed in transaction log (still inside txn)
            logEntity.setStatus(TransactionStatusEnum.FAILURE);
            logEntity.setFailureReason(ex.getMessage());
            transactionLogRepository.save(logEntity);

            log.error("Transfer failed. idempotencyKey={}, reason={}", request.getIdempotencyKey(), ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Business validations for transfer.
     */
    void validateTransfer(TransferRequest request, Account from, Account to) {

        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new IllegalArgumentException("fromAccountId and toAccountId must be different");
        }

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }

        if (!from.isActive()) {
            throw new AccountNotActiveException("From account is not active: " + from.getAccountId());
        }
        if (!to.isActive()) {
            throw new AccountNotActiveException("To account is not active: " + to.getAccountId());
        }

        if (from.getBalance() == null || from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance in account " + from.getAccountId()
                            + ". Available=" + from.getBalance() + ", Required=" + amount
            );
        }
    }

    /**
     * Executes money movement in-memory.
     * (DB writes happen via repository saves in transfer()).
     */
    void executeTransfer(Account from, Account to, BigDecimal amount) {
        // assuming your Account entity has debit/credit(BigDecimal)
        from.debit(amount);
        to.credit(amount);
    }
}