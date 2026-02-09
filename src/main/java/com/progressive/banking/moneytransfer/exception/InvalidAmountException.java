package com.progressive.banking.moneytransfer.exception;


public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(String message) {
        super(message);
    }
}

