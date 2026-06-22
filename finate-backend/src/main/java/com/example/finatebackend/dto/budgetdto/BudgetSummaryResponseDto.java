package com.example.finatebackend.dto.budgetdto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
public record BudgetSummaryResponseDto(
        List<BudgetResponseDto> budgetResponseList,

        BigDecimal totalBudgetAmount,

        BigDecimal totalRemainingBudgetAmount,

        @NotBlank
        BigDecimal totalBudgetSpent

) {
}
