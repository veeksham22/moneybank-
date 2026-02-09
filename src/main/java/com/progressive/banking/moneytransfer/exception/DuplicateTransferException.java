package com.progressive.banking.moneytransfer.exception;

public class DuplicateTransferException extends RuntimeException {
    public DuplicateTransferException(String message) {
        super(message);
    }
}