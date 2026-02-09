package com.progressive.banking.moneytransfer.domain.mapper;

import com.progressive.banking.moneytransfer.domain.dto.AccountResponse;
import com.progressive.banking.moneytransfer.domain.dto.BalanceResponse;
import com.progressive.banking.moneytransfer.domain.entities.Account;

public final class AccountMapper {

    private AccountMapper() {}

    public static AccountResponse toAccountResponse(Account a) {
        return AccountResponse.builder()
                .accountId(a.getAccountId())
                .holderName(a.getHolderName())
                .balance(a.getBalance())
                .status(a.getStatus())
                .version(a.getVersion())
                .lastUpdated(a.getLastUpdated())
                .build();
    }

    public static BalanceResponse toBalanceResponse(Account a) {
        return BalanceResponse.builder()
                .accountId(a.getAccountId())
                .holderName(a.getHolderName())
                .balance(a.getBalance())
                .lastUpdated(a.getLastUpdated())
                .build();
    }
}