package com.example.finatebackend.dto.expensedto;


import com.example.finatebackend.enums.Category;
import com.example.finatebackend.enums.ExpenseTransactionType;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseTransactionResponseDto(
    @NotBlank
    Long expenseTransactionId,

    @NotBlank
    ExpenseTransactionType expenseTransactionType,

    @NotBlank
    BigDecimal expenseAmount,

    @NotBlank
    LocalDate expenseTransactionDate,

    @NotBlank
    Category expenseTransactionCategory,

    String expenseDescription

) {
}
