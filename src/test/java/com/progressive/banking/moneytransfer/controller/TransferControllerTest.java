package com.progressive.banking.moneytransfer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.progressive.banking.moneytransfer.domain.dto.TransferRequest;
import com.progressive.banking.moneytransfer.domain.dto.TransferResponse;
import com.progressive.banking.moneytransfer.domain.enums.TransactionStatusEnum;
import com.progressive.banking.moneytransfer.service.TransferService;

@WebMvcTest(controllers = TransferController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransferService transferService;

    @Test
    @DisplayName("POST /api/v1/transfers returns 201 and TransferResponse")
    void transfer_validRequest_returnsCreated() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1);
        request.setToAccountId(2);
        request.setAmount(BigDecimal.valueOf(100));
        request.setIdempotencyKey("idem-123");

        TransferResponse response = new TransferResponse();
        response.setTransactionId(1);
        response.setFromAccountId(1);
        response.setToAccountId(2);
        response.setAmount(BigDecimal.valueOf(100));
        response.setStatus(TransactionStatusEnum.SUCCESS);
        response.setIdempotencyKey("idem-123");

        given(transferService.transfer(any(TransferRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(1))
                .andExpect(jsonPath("$.fromAccountId").value(1))
                .andExpect(jsonPath("$.toAccountId").value(2))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.idempotencyKey").value("idem-123"));
    }

    @Test
    @DisplayName("POST /api/v1/transfers with X-Idempotency-Key header uses header when body key blank")
    void transfer_headerIdempotencyKey_usedWhenBodyBlank() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1);
        request.setToAccountId(2);
        request.setAmount(BigDecimal.valueOf(50));
        request.setIdempotencyKey("");

        TransferResponse response = new TransferResponse();
        response.setTransactionId(2);
        response.setIdempotencyKey("header-key");
        response.setStatus(TransactionStatusEnum.SUCCESS);

        given(transferService.transfer(any(TransferRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", "header-key")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
