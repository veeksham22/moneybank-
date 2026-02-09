package com.progressive.banking.moneytransfer.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import com.progressive.banking.moneytransfer.domain.dto.AccountResponse;
import com.progressive.banking.moneytransfer.domain.dto.BalanceResponse;
import com.progressive.banking.moneytransfer.domain.dto.TransferResponse;
import com.progressive.banking.moneytransfer.domain.enums.AccountStatusEnum;
import com.progressive.banking.moneytransfer.domain.enums.TransactionStatusEnum;
import com.progressive.banking.moneytransfer.service.AccountService;

/**
 * Slice tests for {@link AccountController} using JUnit and MockMvc.
 */
@WebMvcTest(controllers = AccountController.class)
@AutoConfigureMockMvc(addFilters = false) // disable security filters for controller tests
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Test
    @DisplayName("GET /api/v1/accounts/{id} returns account details")
    void getAccount_shouldReturnAccountResponse() throws Exception {
        Integer id = 1;
        AccountResponse response = new AccountResponse();
        response.setAccountId(id);
        response.setHolderName("John Doe");
        response.setBalance(BigDecimal.valueOf(500));
        response.setStatus(AccountStatusEnum.ACTIVE);
        response.setVersion(1L);
        response.setLastUpdated(LocalDateTime.now());

        given(accountService.getAccount(eq(id))).willReturn(response);

        mockMvc.perform(get("/api/v1/accounts/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(id))
                .andExpect(jsonPath("$.holderName").value("John Doe"))
                .andExpect(jsonPath("$.balance").value(500));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id}/balance returns balance")
    void getBalance_shouldReturnBalanceResponse() throws Exception {
        Integer id = 2;
        BalanceResponse response = new BalanceResponse();
        response.setAccountId(id);
        response.setBalance(BigDecimal.valueOf(250));

        given(accountService.getBalance(eq(id))).willReturn(response);

        mockMvc.perform(get("/api/v1/accounts/{id}/balance", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(id))
                .andExpect(jsonPath("$.balance").value(250));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id}/transactions returns transactions list")
    void getTransactions_shouldReturnTransferResponseList() throws Exception {
        Integer id = 3;
        TransferResponse tx = new TransferResponse();
        tx.setFromAccountId(id);
        tx.setToAccountId(4);
        tx.setAmount(BigDecimal.valueOf(100));
        tx.setStatus(TransactionStatusEnum.SUCCESS);

        given(accountService.getTransactions(eq(id))).willReturn(Collections.singletonList(tx));

        mockMvc.perform(get("/api/v1/accounts/{id}/transactions", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromAccountId").value(id))
                .andExpect(jsonPath("$[0].toAccountId").value(4))
                .andExpect(jsonPath("$[0].amount").value(100));
    }
}

