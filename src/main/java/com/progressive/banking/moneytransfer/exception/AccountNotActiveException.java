package com.progressive.banking.moneytransfer.exception;

public class AccountNotActiveException  extends RuntimeException {
    public AccountNotActiveException(String message) {
        super(message);
    }

}
