package com.progressive.banking.moneytransfer.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.progressive.banking.moneytransfer.domain.enums.TransactionStatusEnum;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "transaction_log",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_txn_idempotency_key", columnNames = "idempotencyKey")
    },
    indexes = {
        @Index(name = "idx_txn_from_account", columnList = "fromAccountId"),
        @Index(name = "idx_txn_to_account", columnList = "toAccountId"),
        @Index(name = "idx_txn_status", columnList = "status"),
        @Index(name = "idx_txn_created_on", columnList = "createdOn")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionId;

    @Column(nullable = false)
    private Integer fromAccountId;

    @Column(nullable = false)
    private Integer toAccountId;

    // Use BigDecimal for money to avoid precision errors
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatusEnum status;

    @Column(length = 255)
    private String failureReason;

    // Fixed typo: ideompotencyKey -> idempotencyKey
    @Column(nullable = false, length = 64)
    private String idempotencyKey;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @PrePersist
    void onCreate() {
        if (createdOn == null) {
            createdOn = LocalDateTime.now();
        }
        if (status == null) {
            status = TransactionStatusEnum.PENDING;
        }
    }

    /* ---------------- Domain helper methods ---------------- */

    public boolean isPending() {
        return this.status == TransactionStatusEnum.PENDING;
    }

    public boolean isSuccess() {
        return this.status == TransactionStatusEnum.SUCCESS;
    }

    public boolean isFailed() {
        return this.status == TransactionStatusEnum.FAILURE;
    }

    public void markSuccess() {
        this.status = TransactionStatusEnum.SUCCESS;
        this.failureReason = null;
    }

    public void markFailed(String reason) {
        this.status = TransactionStatusEnum.FAILURE;
        this.failureReason = (reason == null || reason.isBlank()) ? "Unknown failure" : reason;
    }
}