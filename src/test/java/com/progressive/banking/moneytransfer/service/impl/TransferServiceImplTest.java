package com.progressive.banking.moneytransfer.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.progressive.banking.moneytransfer.domain.dto.TransferRequest;
import com.progressive.banking.moneytransfer.domain.entities.Account;
import com.progressive.banking.moneytransfer.domain.enums.AccountStatusEnum;
import com.progressive.banking.moneytransfer.exception.AccountNotActiveException;
import com.progressive.banking.moneytransfer.exception.InsufficientBalanceException;

/**
 * Pure unit tests for {@link TransferServiceImpl} business rules using JUnit 5.
 *
 * These tests focus on {@code validateTransfer} and {@code executeTransfer}
 * which contain the core money‑movement rules.
 */
class TransferServiceImplTest {

    // We don't need repositories for these tests; methods under test don't use them.
    private final TransferServiceImpl transferService =
            new TransferServiceImpl(null, null);

    private Account activeAccount(Integer id, BigDecimal balance) {
        Account acc = new Account();
        acc.setAccountId(id);
        acc.setHolderName("Holder " + id);
        acc.setBalance(balance);
        acc.setStatus(AccountStatusEnum.ACTIVE);
        return acc;
    }

    @Test
    @DisplayName("validateTransfer passes for valid request and active accounts")
    void validateTransfer_whenValid_shouldPass() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1);
        request.setToAccountId(2);
        request.setAmount(BigDecimal.valueOf(100));

        Account from = activeAccount(1, BigDecimal.valueOf(500));
        Account to = activeAccount(2, BigDecimal.valueOf(200));

        // Expect no exception
        transferService.validateTransfer(request, from, to);
    }

    @Test
    @DisplayName("validateTransfer throws when from and to accounts are the same")
    void validateTransfer_sameAccount_throwsIllegalArgumentException() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1);
        request.setToAccountId(1);
        request.setAmount(BigDecimal.valueOf(50));

        Account from = activeAccount(1, BigDecimal.valueOf(500));
        Account to = activeAccount(1, BigDecimal.valueOf(500));

        assertThrows(IllegalArgumentException.class,
                () -> transferService.validateTransfer(request, from, to));
    }

    @Test
    @DisplayName("validateTransfer throws when amount is zero or negative")
    void validateTransfer_nonPositiveAmount_throwsIllegalArgumentException() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1);
        request.setToAccountId(2);
        request.setAmount(BigDecimal.ZERO);

        Account from = activeAccount(1, BigDecimal.valueOf(500));
        Account to = activeAccount(2, BigDecimal.valueOf(200));

        assertThrows(IllegalArgumentException.class,
                () -> transferService.validateTransfer(request, from, to));
    }

    @Test
    @DisplayName("validateTransfer throws when from account is inactive")
    void validateTransfer_inactiveFromAccount_throwsAccountNotActiveException() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1);
        request.setToAccountId(2);
        request.setAmount(BigDecimal.valueOf(50));

        Account from = activeAccount(1, BigDecimal.valueOf(500));
        from.setStatus(AccountStatusEnum.LOCKED);

        Account to = activeAccount(2, BigDecimal.valueOf(200));

        assertThrows(AccountNotActiveException.class,
                () -> transferService.validateTransfer(request, from, to));
    }

    @Test
    @DisplayName("validateTransfer throws when balance is insufficient")
    void validateTransfer_insufficientBalance_throwsInsufficientBalanceException() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1);
        request.setToAccountId(2);
        request.setAmount(BigDecimal.valueOf(1000));

        Account from = activeAccount(1, BigDecimal.valueOf(100));
        Account to = activeAccount(2, BigDecimal.valueOf(200));

        assertThrows(InsufficientBalanceException.class,
                () -> transferService.validateTransfer(request, from, to));
    }

    @Test
    @DisplayName("executeTransfer debits from source and credits destination")
    void executeTransfer_shouldMoveMoneyBetweenAccounts() {
        Account from = activeAccount(1, BigDecimal.valueOf(500));
        Account to = activeAccount(2, BigDecimal.valueOf(200));
        BigDecimal amount = BigDecimal.valueOf(150);

        transferService.executeTransfer(from, to, amount);

        assertEquals(BigDecimal.valueOf(350), from.getBalance());
        assertEquals(BigDecimal.valueOf(350), to.getBalance());
    }
}


