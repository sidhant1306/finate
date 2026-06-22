package com.example.finatebackend.schedulers;

import com.example.finatebackend.dao.BudgetRepository;
import com.example.finatebackend.model.Budget;
import com.example.finatebackend.model.User;
import com.example.finatebackend.security.SecurityUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
@Service
public class BudgetScheduler {

    private final BudgetRepository budgetRepository;

    public BudgetScheduler(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public void resetMonthlyBudgetSpent() {
        List<Budget> budgets = budgetRepository.findAll();
            budgets.forEach(budget -> budget.setBudgetSpent(BigDecimal.ZERO));
            budgetRepository.saveAll(budgets);
    }
}
