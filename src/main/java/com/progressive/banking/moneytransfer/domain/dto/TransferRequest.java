package com.progressive.banking.moneytransfer.domain.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotNull(message = "fromAccountId must not be null")
    private Integer fromAccountId;

    @NotNull(message = "toAccountId must not be null")
    private Integer toAccountId;

    @NotNull(message = "amount must not be null")
    @Positive(message = "amount must be greater than zero")
    private BigDecimal amount;

    /**
     * key — ensures retry requests are not processed twice
     * Recommend using UUID string.
     */
    //@NotNull(message = "idempotencyKey must not be blank")
    @Size(max = 64, message = "idempotencyKey must not exceed 64 characters")
    private String idempotencyKey;

    /**
     * Optional: A description for the transaction (e.g., "Rent payment", "Transfer to savings")
     */
    @Size(max = 120, message = "description cannot exceed 120 characters")
    private String description;
}