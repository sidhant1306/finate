package com.example.finatebackend.dto.dashboarddto;

import com.example.finatebackend.dto.expensedto.ExpenseTransactionResponseDto;
import java.math.BigDecimal;
import java.util.List;

public record DashboardResponseDto(
        BigDecimal walletBalance,
        BigDecimal netWorth,
        BigDecimal totalBudgetRemaining,
        BigDecimal totalDebitAmountLast30Days,
        BigDecimal totalCreditAmountLast30Days,
        List<ExpenseTransactionResponseDto> last30DaysExpenseTransactionList
) {
}
