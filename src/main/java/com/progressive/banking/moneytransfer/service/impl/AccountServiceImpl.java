package com.progressive.banking.moneytransfer.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.progressive.banking.moneytransfer.domain.dto.AccountResponse;
import com.progressive.banking.moneytransfer.domain.dto.BalanceResponse;
import com.progressive.banking.moneytransfer.domain.dto.TransferResponse;
import com.progressive.banking.moneytransfer.domain.entities.Account;
import com.progressive.banking.moneytransfer.domain.entities.TransactionLog;
import com.progressive.banking.moneytransfer.domain.mapper.AccountMapper;
import com.progressive.banking.moneytransfer.domain.mapper.TransferMapper;
import com.progressive.banking.moneytransfer.exception.AccountNotFoundException;
import com.progressive.banking.moneytransfer.repository.AccountRepository;
import com.progressive.banking.moneytransfer.repository.TransactionLogRepository;
import com.progressive.banking.moneytransfer.service.AccountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionLogRepository transactionLogRepository;

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccount(Integer id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id));
        return AccountMapper.toAccountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Integer id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id));
        return AccountMapper.toBalanceResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferResponse> getTransactions(Integer id) {

        // validates account existence
        accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id));

        List<TransactionLog> logs = transactionLogRepository
                .findByFromAccountIdOrToAccountIdOrderByCreatedOnDesc(id, id);

        return logs.stream().map(TransferMapper::toResponse).toList();
    }
}