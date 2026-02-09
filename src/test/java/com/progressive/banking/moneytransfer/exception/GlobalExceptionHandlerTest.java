package com.progressive.banking.moneytransfer.exception;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.progressive.banking.moneytransfer.controller.AccountController;
import com.progressive.banking.moneytransfer.service.AccountService;

@WebMvcTest(controllers = AccountController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Test
    @DisplayName("AccountNotFoundException returns 404 and ACC-404 error body")
    void accountNotFound_returns404AndErrorResponse() throws Exception {
        when(accountService.getAccount(999)).thenThrow(new AccountNotFoundException("Account not found: 999"));

        mockMvc.perform(get("/api/v1/accounts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ACC-404"))
                .andExpect(jsonPath("$.message").value("Account not found: 999"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    @DisplayName("AccountNotActiveException returns 403 and ACC-403 error body")
    void accountNotActive_returns403AndErrorResponse() throws Exception {
        when(accountService.getBalance(1)).thenThrow(new AccountNotActiveException("Account is not active: 1"));

        mockMvc.perform(get("/api/v1/accounts/1/balance"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACC-403"))
                .andExpect(jsonPath("$.message").value("Account is not active: 1"));
    }

    @Test
    @DisplayName("InsufficientBalanceException returns 400 and TRX-400 error body")
    void insufficientBalance_returns400AndErrorResponse() throws Exception {
        when(accountService.getAccount(1)).thenThrow(
                new InsufficientBalanceException("Insufficient balance in account 1. Available=10, Required=100"));

        mockMvc.perform(get("/api/v1/accounts/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("TRX-400"))
                .andExpect(jsonPath("$.message").value("Insufficient balance in account 1. Available=10, Required=100"));
    }
}
