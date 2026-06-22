package com.example.finatebackend.service;

import com.example.finatebackend.dao.BudgetRepository;
import com.example.finatebackend.dao.ExpenseTransactionRepository;
import com.example.finatebackend.dto.budgetdto.BudgetRequestDto;
import com.example.finatebackend.dto.budgetdto.BudgetResponseDto;
import com.example.finatebackend.dto.budgetdto.BudgetSummaryResponseDto;
import com.example.finatebackend.enums.Category;
import com.example.finatebackend.enums.ExpenseTransactionType;
import com.example.finatebackend.exceptions.BudgetAlreadyExistsException;
import com.example.finatebackend.exceptions.BudgetNotFoundException;
import com.example.finatebackend.exceptions.AccessDeniedException;
import com.example.finatebackend.model.Budget;
import com.example.finatebackend.model.ExpenseTransaction;
import com.example.finatebackend.model.User;
import com.example.finatebackend.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseTransactionRepository expenseTransactionRepository;

    public BudgetService(BudgetRepository budgetRepository, ExpenseTransactionRepository expenseTransactionRepository) {
        this.budgetRepository = budgetRepository;
        this.expenseTransactionRepository = expenseTransactionRepository;
    }

    public BudgetResponseDto createBudget(BudgetRequestDto budgetRequestDto) {
        User user = SecurityUtils.getCurrentUser();

        Budget exists = budgetRepository.findByUserUserIdAndBudgetCategory(user.getUserId(), budgetRequestDto.budgetCategory());

        if(exists != null) {
            throw new BudgetAlreadyExistsException("Budget already exists for category : " +budgetRequestDto.budgetCategory());
        }

        // search in the transaction repo if there is any debit made by the user already for this budget category in the current month
        List<ExpenseTransaction> transactions = expenseTransactionRepository.findAllByUserUserIdAndExpenseTransactionCategoryAndExpenseTransactionDateBetweenAndExpenseTransactionType(user.getUserId(), budgetRequestDto.budgetCategory(), LocalDate.now().minusMonths(1), LocalDate.now(), ExpenseTransactionType.DEBIT);
        // this is the amount already spent by the user in the current month for this budget category
        BigDecimal alreadySpentAmount = BigDecimal.ZERO;

        // if any debit transactions are found for the user in the current month for this budget category, calculate the total amount spent
        if(transactions != null) {
            alreadySpentAmount = transactions.stream()
                    .map(ExpenseTransaction::getExpenseAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        Budget budget = Budget.builder()
                .user(user)
                .budgetAmount(budgetRequestDto.budgetAmount())
                .budgetCategory(budgetRequestDto.budgetCategory())
                .budgetSpent(alreadySpentAmount)
                .build();
        budgetRepository.save(budget);



        return new BudgetResponseDto(
                budget.getBudgetId(),
                budget.getBudgetAmount(),
                budget.getBudgetCategory(),
                budget.getBudgetAmount().subtract(budget.getBudgetSpent()),
                alreadySpentAmount);
    }


    public BudgetSummaryResponseDto getAllBudgets() {
        User user = SecurityUtils.getCurrentUser();

        List<Budget> list = budgetRepository.findAllByUserUserId(user.getUserId());

        List<BudgetResponseDto> budgetResponseDtoList = new ArrayList<>();
        BigDecimal totalBudgetAmount = BigDecimal.ZERO;
        BigDecimal totalBudgetSpent = BigDecimal.ZERO;

        for(Budget budget : list) {
            budgetResponseDtoList.add(new BudgetResponseDto(
                    budget.getBudgetId(),
                    budget.getBudgetAmount(),
                    budget.getBudgetCategory(),
                    budget.getBudgetAmount().subtract(budget.getBudgetSpent()),
                    budget.getBudgetSpent()
            ));
            totalBudgetAmount = totalBudgetAmount.add(budget.getBudgetAmount());
            totalBudgetSpent = totalBudgetSpent.add(budget.getBudgetSpent());
        }

        BigDecimal totalRemainingBudgetAmount = totalBudgetAmount.subtract(totalBudgetSpent);

        return new BudgetSummaryResponseDto(
               budgetResponseDtoList,
                totalBudgetAmount,
                totalRemainingBudgetAmount,
                totalBudgetSpent
        );
    }

    public BudgetResponseDto editBudget(Long budgetId, BudgetRequestDto budgetRequestDto) {
        User user = SecurityUtils.getCurrentUser();

        Budget budget = budgetRepository.findById(budgetId).orElseThrow(
                () ->  new BudgetNotFoundException("Budget with provided id : " + budgetId +" does not exist")
        );

        if(!(budget.getUser().getUserId().equals(user.getUserId()))) {
            throw new AccessDeniedException("You are not authorized to perform this action");
        }

        budget.setBudgetAmount(budgetRequestDto.budgetAmount());
        budget.setBudgetCategory(budgetRequestDto.budgetCategory());

        budgetRepository.save(budget);

        return new BudgetResponseDto(
                budget.getBudgetId(),
                budget.getBudgetAmount(),
                budget.getBudgetCategory(),
                budget.getBudgetAmount().subtract(budget.getBudgetSpent()),
                budget.getBudgetSpent()
        );
    }

    public String deleteBudget(Long budgetId) {
        User user = SecurityUtils.getCurrentUser();

        Budget budget = budgetRepository.findById(budgetId).orElseThrow(
                () ->  new BudgetNotFoundException("Budget with provided id : " + budgetId +" does not exist")
        );

        if(!(budget.getUser().getUserId().equals(user.getUserId()))) {
            throw new AccessDeniedException("You are not authorized to perform this action");
        }

        budgetRepository.deleteById(budgetId);
        return "Budget deleted Successfully";
    }

    public BudgetResponseDto getBudgetByCategory(Category category) {
        User user = SecurityUtils.getCurrentUser();

        Budget budget = budgetRepository.findByUserUserIdAndBudgetCategory(user.getUserId(), category);
        if(budget == null) {
            throw new BudgetNotFoundException("Budget with category: " +category + " not found");
        }

        return new BudgetResponseDto(
                budget.getBudgetId(),
                budget.getBudgetAmount(),
                budget.getBudgetCategory(),
                budget.getBudgetAmount().subtract(budget.getBudgetSpent()),
                budget.getBudgetSpent()
        );
    }

    public BudgetResponseDto getBudgetById(Long budgetId) {
        User user = SecurityUtils.getCurrentUser();

        Budget budget = budgetRepository.findByUserUserIdAndBudgetId(user.getUserId(), budgetId);
        if(budget == null) {
            throw new BudgetNotFoundException("Budget with budgetId: " +budgetId + " not found");
        }

        return new BudgetResponseDto(
                budget.getBudgetId(),
                budget.getBudgetAmount(),
                budget.getBudgetCategory(),
                budget.getBudgetAmount().subtract(budget.getBudgetSpent()),
                budget.getBudgetSpent()
        );
    }

    public String deleteAllBudgets() {
        User user = SecurityUtils.getCurrentUser();
        List<Budget> allBudgets = budgetRepository.findAllByUserUserId(user.getUserId());

        budgetRepository.deleteAll(allBudgets);

        return "All budgets successfully removed";
    }
}
