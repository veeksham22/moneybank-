package com.progressive.banking.moneytransfer.repository;

import java.util.Optional;

import com.progressive.banking.moneytransfer.domain.entities.Account;
import com.progressive.banking.moneytransfer.domain.enums.AccountStatusEnum;

import com.progressive.banking.moneytransfer.domain.entities.Account;
import com.progressive.banking.moneytransfer.domain.enums.AccountStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    /**
     * Optional: Find account with pessimistic write lock
     * Use ONLY if you decide to use pessimistic locking in transfer.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountId = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Integer id);

    Optional<Account> findByHolderName(String holderName);

    boolean existsByAccountIdAndStatus(Integer accountId, AccountStatusEnum status);

    long countByStatus(AccountStatusEnum status);
}