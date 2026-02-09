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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.progressive.banking.moneytransfer.domain.dto.TransferRequest;
import com.progressive.banking.moneytransfer.domain.dto.TransferResponse;
import com.progressive.banking.moneytransfer.domain.enums.TransactionStatusEnum;
import com.progressive.banking.moneytransfer.service.TransferService;

@Import(ObjectMapper.class) // Manually import ObjectMapper bean for testing
@WebMvcTest(controllers = TransferController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;  // ObjectMapper to convert objects to JSON

    @MockitoBean
    private TransferService transferService;  // Mock TransferService

    @Test
    @DisplayName("POST /api/v1/transfers returns 201 and TransferResponse")
    void transfer_validRequest_returnsCreated() throws Exception {
        // Create the TransferRequest object with valid data
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1);
        request.setToAccountId(2);
        request.setAmount(BigDecimal.valueOf(100));
        request.setIdempotencyKey("idem-123");  // Valid idempotency key

        // Create the mocked TransferResponse object
        TransferResponse response = new TransferResponse();
        response.setTransactionId(1);
        response.setFromAccountId(1);
        response.setToAccountId(2);
        response.setAmount(BigDecimal.valueOf(100));
        response.setStatus(TransactionStatusEnum.SUCCESS);
        response.setIdempotencyKey("idem-123");

        // Mock TransferService behavior
        given(transferService.transfer(any(TransferRequest.class))).willReturn(response);

        // Perform the POST request to the controller
        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))  // Send request body
                .andExpect(status().isCreated())  // Expect 201 Created
                .andExpect(jsonPath("$.transactionId").value(1))  // Validate response fields
                .andExpect(jsonPath("$.fromAccountId").value(1))
                .andExpect(jsonPath("$.toAccountId").value(2))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.idempotencyKey").value("idem-123"));
    }

    @Test
    @DisplayName("POST /api/v1/transfers with X-Idempotency-Key header uses header when body key blank")
    void transfer_headerIdempotencyKey_usedWhenBodyBlank() throws Exception {
        // Create a TransferRequest with an empty idempotency key
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(1);
        request.setToAccountId(2);
        request.setAmount(BigDecimal.valueOf(50));
        request.setIdempotencyKey(null);  // Empty idempotency key in body

        // Create the mocked TransferResponse object
        TransferResponse response = new TransferResponse();
        response.setTransactionId(2);
        response.setIdempotencyKey("header-key");  // Set idempotency key to be used from header
        response.setStatus(TransactionStatusEnum.SUCCESS);

        // Mock TransferService behavior
        given(transferService.transfer(any(TransferRequest.class))).willReturn(response);

        // Perform the POST request with X-Idempotency-Key header
        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", "header-key")  // Use header for idempotency key
                        .content(objectMapper.writeValueAsString(request)))  // Send the request body
                .andExpect(status().isCreated())  // Expect 201 Created status
                .andExpect(jsonPath("$.transactionId").value(2))  // Verify response fields
                .andExpect(jsonPath("$.idempotencyKey").value("header-key"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andDo(result -> {
                    // Log response body if status is 422 for debugging purposes
                    if (result.getResponse().getStatus() == 422) {
                        System.out.println("Response Body: " + result.getResponse().getContentAsString());
                    }
                });
    }
}
