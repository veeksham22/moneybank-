package com.progressive.banking.moneytransfer.service;

import com.progressive.banking.moneytransfer.domain.dto.TransferRequest;
import com.progressive.banking.moneytransfer.domain.dto.TransferResponse;

public interface TransferService {
    TransferResponse transfer(TransferRequest request);
}
