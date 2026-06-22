package com.example.finatebackend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(BudgetAlreadyExistsException.class)
    public ResponseEntity<String> handleBudgetAlreadyExistsException(BudgetAlreadyExistsException e) {
         return ResponseEntity.badRequest().body(e.getMessage());
    }

     @ExceptionHandler(BudgetNotFoundException.class)
     public ResponseEntity<String> handleBudgetNotFoundException(BudgetNotFoundException e) {
         return ResponseEntity.badRequest().body(e.getMessage());
     }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleNotAuthenticatedException(AccessDeniedException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(NotFoundException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(PaymentVerificationException.class)
    public ResponseEntity<String> handlePaymentVerificationException(PaymentVerificationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<String> handleInsufficientFundsException(InsufficientFundsException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(NoStockHoldingException.class)
    public ResponseEntity<String> handleNoStockHoldingException(NoStockHoldingException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<String> handleDuplicateException(DuplicateException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.internalServerError().body("An unexpected error occurred: " + e.getMessage());
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<String> handleEmailNotVerifiedException(EmailNotVerifiedException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatusCode.valueOf(401)).body("An unexpected error occurred: " + e.getMessage());
    }

}
