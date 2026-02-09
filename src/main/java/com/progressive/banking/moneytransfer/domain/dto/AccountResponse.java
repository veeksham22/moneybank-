package com.progressive.banking.moneytransfer.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.progressive.banking.moneytransfer.domain.enums.AccountStatusEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    @NotNull(message = "accountId must not be null")
    private Integer accountId;

    @NotBlank(message = "holderName must not be blank")
    @Size(min = 2, max = 80, message = "holderName must be between 2 and 80 characters")
    private String holderName;

    @NotNull(message = "balance must not be null")
    @PositiveOrZero(message = "balance must be zero or positive")
    private BigDecimal balance;

    @NotNull(message = "status must not be null")
    private AccountStatusEnum status;

    /**
     * Optional: version can be useful for optimistic locking / debugging.
     * If your Account entity uses @Version Long version, keep it as Long.
     */
    @NotNull(message = "version must not be null")
    private Long version;

    @NotNull(message = "lastUpdated must not be null")
    private LocalDateTime lastUpdated;
}
