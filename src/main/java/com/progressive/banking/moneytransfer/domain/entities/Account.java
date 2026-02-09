package com.progressive.banking.moneytransfer.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.progressive.banking.moneytransfer.domain.enums.AccountStatusEnum;
import com.progressive.banking.moneytransfer.exception.AccountNotActiveException;
import com.progressive.banking.moneytransfer.exception.InsufficientBalanceException;
import com.progressive.banking.moneytransfer.exception.InvalidAmountException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    private Integer accountId;

    @Column(nullable = false)
    private String holderName;


    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatusEnum status = AccountStatusEnum.ACTIVE;


    @Version
    private Long version;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    public void prePersist() {
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        if (status == null) {
            status = AccountStatusEnum.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == AccountStatusEnum.ACTIVE;
    }

    public void debit(BigDecimal amount) {
        validateAmount(amount);
        ensureActive();

        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: " + balance + ", Requested: " + amount
            );
        }

        this.balance = this.balance.subtract(amount);
        this.lastUpdated = LocalDateTime.now();
    }

    public void credit(BigDecimal amount) {
        validateAmount(amount);
        ensureActive();

        this.balance = this.balance.add(amount);
        this.lastUpdated = LocalDateTime.now();
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAmountException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
    }

    private void ensureActive() {
        if (!isActive()) {
            throw new AccountNotActiveException("Account is not active. Status: " + status);
        }
    }
}