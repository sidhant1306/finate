package com.example.finatebackend.dto.budgetdto;

import com.example.finatebackend.enums.Category;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record BudgetRequestDto(
        @NotBlank
        BigDecimal budgetAmount,

        @NotBlank
        Category budgetCategory
) {
}
