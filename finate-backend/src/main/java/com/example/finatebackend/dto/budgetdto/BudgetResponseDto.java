package com.example.finatebackend.dto.budgetdto;

import com.example.finatebackend.enums.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.math.BigDecimal;
public record BudgetResponseDto(
        @NotBlank
        Long budgetId,

        @NotBlank
        BigDecimal budgetAmount,

        @NotBlank
        Category budgetCategory,

        @NotBlank
        BigDecimal remainingBudget,

        @NotBlank
        BigDecimal budgetSpent
) {

}
