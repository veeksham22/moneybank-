package com.progressive.banking.moneytransfer.service;

import java.util.List;

import com.progressive.banking.moneytransfer.domain.dto.AccountResponse;
import com.progressive.banking.moneytransfer.domain.dto.BalanceResponse;
import com.progressive.banking.moneytransfer.domain.dto.TransferResponse;

public interface AccountService {

    AccountResponse getAccount(Integer id);

    BalanceResponse getBalance(Integer id);

    List<TransferResponse> getTransactions(Integer id);
}