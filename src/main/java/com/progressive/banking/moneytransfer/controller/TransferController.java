package com.progressive.banking.moneytransfer.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.progressive.banking.moneytransfer.domain.dto.TransferRequest;
import com.progressive.banking.moneytransfer.domain.dto.TransferResponse;
import com.progressive.banking.moneytransfer.service.TransferService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Validated
public class TransferController {

    private final TransferService transferService;

    /**
     * Execute fund transfer
     * POST /api/v1/transfers
     */
    @PostMapping
    // Optional: enable if you are using method-level security
    // @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyHeader,
            Authentication authentication) {

        // If client sends idempotency key in header, prefer it.
        // If not present in either, generate one (optional, but recommended to require it).
        if ((request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank())
                && idempotencyHeader != null && !idempotencyHeader.isBlank()) {
            request.setIdempotencyKey(idempotencyHeader);
        }

        if (request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()) {
            // You can also throw InvalidAmountException or IllegalArgumentException; your ControllerAdvice handles it.
            request.setIdempotencyKey(UUID.randomUUID().toString());
        }

        // Authentication info is available if needed:
        // String user = (authentication != null) ? authentication.getName() : "anonymous";

        TransferResponse response = transferService.transfer(request);

        // 201 is common when creating a transaction record
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}