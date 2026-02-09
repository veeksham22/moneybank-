package com.progressive.banking.moneytransfer.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {

    private Integer accountId;

    private String holderName;

    private BigDecimal balance;

    private LocalDateTime lastUpdated;
}