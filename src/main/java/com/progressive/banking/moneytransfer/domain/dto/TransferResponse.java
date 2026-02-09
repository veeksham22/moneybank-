package com.progressive.banking.moneytransfer.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.progressive.banking.moneytransfer.domain.enums.TransactionStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    private Integer transactionId;

    private Integer fromAccountId;

    private Integer toAccountId;

    private BigDecimal amount;

    private TransactionStatusEnum status;

    private String failureReason;

    private String idempotencyKey;

    private LocalDateTime createdOn;
}