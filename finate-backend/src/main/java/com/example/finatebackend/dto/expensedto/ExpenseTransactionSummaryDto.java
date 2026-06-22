package com.example.finatebackend.dto.expensedto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;

public record ExpenseTransactionSummaryDto(
        List<ExpenseTransactionResponseDto> expenseTransactionResponseDtoList,

        @NotBlank
        BigDecimal currentBalance,

        @NotBlank
        BigDecimal totalDebitAmount,

        @NotBlank
        BigDecimal totalCreditAmount
) {
}
