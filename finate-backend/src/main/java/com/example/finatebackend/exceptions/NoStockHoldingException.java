package com.example.finatebackend.exceptions;

public class NoStockHoldingException extends RuntimeException {
    public NoStockHoldingException(String message) {
        super(message);
    }
}
