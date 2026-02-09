package com.progressive.banking.moneytransfer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.progressive.banking.moneytransfer.domain.entities.TransactionLog;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, Integer> {

    // Used for idempotency handling
    Optional<TransactionLog> findByIdempotencyKey(String idempotencyKey);

    // Used to show all transactions for an account (sent or received)
    List<TransactionLog> findByFromAccountIdOrToAccountIdOrderByCreatedOnDesc(Integer fromId, Integer toId);
}
