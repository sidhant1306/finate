package com.example.finatebackend.exceptions;

public class BudgetAlreadyExistsException extends RuntimeException {
    public BudgetAlreadyExistsException(String message) {
        super(message);
    }
}
