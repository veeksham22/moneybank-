package com.progressive.banking.moneytransfer.domain.mapper;

import com.progressive.banking.moneytransfer.domain.dto.TransferResponse;
import com.progressive.banking.moneytransfer.domain.entities.TransactionLog;

public final class TransferMapper {

    private TransferMapper() {}

    public static TransferResponse toResponse(TransactionLog log) {
        return TransferResponse.builder()
                .transactionId(log.getTransactionId())
                .fromAccountId(log.getFromAccountId())
                .toAccountId(log.getToAccountId())
                .amount(log.getAmount())
                .status(log.getStatus())
                .failureReason(log.getFailureReason())
                .idempotencyKey(log.getIdempotencyKey())
                .createdOn(log.getCreatedOn())
                .build();
    }
}