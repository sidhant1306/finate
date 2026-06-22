package com.example.finatebackend.dto.expensedto;

import com.example.finatebackend.enums.Category;
import com.example.finatebackend.enums.ExpenseTransactionType;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;


public record ExpenseTransactionRequestDto(
        @NotBlank
        ExpenseTransactionType expenseTransactionType,

        @NotBlank
        BigDecimal expenseAmount,

        @NotBlank
        Category expenseCategory,

        String expenseDescription
) {
}
