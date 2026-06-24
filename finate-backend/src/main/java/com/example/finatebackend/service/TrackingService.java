package com.example.finatebackend.service;

import com.example.finatebackend.dao.BudgetRepository;
import com.example.finatebackend.dao.ExpenseTransactionRepository;
import com.example.finatebackend.dao.UserRepository;
import com.example.finatebackend.dto.expensedto.ExpenseTransactionRequestDto;
import com.example.finatebackend.dto.expensedto.ExpenseTransactionResponseDto;
import com.example.finatebackend.dto.expensedto.ExpenseTransactionSummaryDto;
import com.example.finatebackend.enums.Category;
import com.example.finatebackend.enums.ExpenseTransactionType;
import com.example.finatebackend.exceptions.AccessDeniedException;
import com.example.finatebackend.exceptions.NotFoundException;
import com.example.finatebackend.model.Budget;
import com.example.finatebackend.model.ExpenseTransaction;
import com.example.finatebackend.model.User;
import com.example.finatebackend.security.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrackingService {

    private final ExpenseTransactionRepository expenseTransactionRepository;
    private final BudgetRepository budgetRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public TrackingService(ExpenseTransactionRepository expenseTransactionRepository, BudgetRepository budgetRepository, SimpMessagingTemplate messagingTemplate, UserRepository userRepository) {
        this.expenseTransactionRepository = expenseTransactionRepository;
        this.budgetRepository = budgetRepository;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }
    @Transactional
    @CacheEvict(value = "userFinancialContext", key = "@securityUtils.getCurrentUser().getUserId()")
    public ExpenseTransactionResponseDto createExpenseTransaction(ExpenseTransactionRequestDto expenseTransactionRequestDto) {
        User user = SecurityUtils.getCurrentUser();
        ExpenseTransaction transaction = ExpenseTransaction.builder()
                .expenseTransactionCategory(expenseTransactionRequestDto.expenseCategory())
                .expenseTransactionDate(LocalDate.now())
                .expenseTransactionType(expenseTransactionRequestDto.expenseTransactionType())
                .expenseAmount(expenseTransactionRequestDto.expenseAmount())
                .expenseDescription(expenseTransactionRequestDto.expenseDescription())
                .user(user)
                .build();
        expenseTransactionRepository.save(transaction);
        Budget budget = budgetRepository.findByUserUserIdAndBudgetCategory(user.getUserId(), expenseTransactionRequestDto.expenseCategory());
        // check if the transaction type is debit and the budget exists,
        // if it does, then update the budget-spent amount :
        if(transaction.getExpenseTransactionType() == ExpenseTransactionType.DEBIT && budget != null) {
            updateBudgetSpent(
                    user.getUserId(),
                    ExpenseTransactionType.DEBIT,
                    transaction.getExpenseTransactionCategory(),
                    transaction.getExpenseAmount()
            );
        }
        return new ExpenseTransactionResponseDto(
                transaction.getExpenseTransactionId(),
                transaction.getExpenseTransactionType(),
                transaction.getExpenseAmount(),
                transaction.getExpenseTransactionDate(),
                transaction.getExpenseTransactionCategory(),
                transaction.getExpenseDescription()
        );
    }
    @Transactional
    @CacheEvict(value = "userFinancialContext", key = "@securityUtils.getCurrentUser().getUserId()")
    public String deleteExpenseTransactionById(Long expenseTransactionId) {
        User user = SecurityUtils.getCurrentUser();
        ExpenseTransaction transaction = expenseTransactionRepository.findByExpenseTransactionId(expenseTransactionId).orElseThrow(
                () -> new NotFoundException("expense with id : " +expenseTransactionId + " not found")
        );
        if(!(transaction.getUser().getUserId().equals(user.getUserId()))) {
            throw new AccessDeniedException("You are not authorized to delete this transaction, " +user.getFirstName());
        }

        boolean isCurrentMonth =
        transaction.getExpenseTransactionDate().getMonthValue() == LocalDate.now().getMonthValue() &&
        transaction.getExpenseTransactionDate().getYear() == LocalDate.now().getYear();


        if(isCurrentMonth) {
            if (transaction.getExpenseTransactionType() == ExpenseTransactionType.DEBIT) {
                updateBudgetSpent(
                        user.getUserId(),
                        ExpenseTransactionType.CREDIT,
                        transaction.getExpenseTransactionCategory(),
                        transaction.getExpenseAmount());
            }
        }
        expenseTransactionRepository.delete(transaction);
        return "Transaction deleted successfully";
    }

    public ExpenseTransactionSummaryDto getAllExpenses(LocalDate startDate, LocalDate endDate) {
        User user = SecurityUtils.getCurrentUser();
        List<ExpenseTransaction> transactionList;
        List<Budget> budgetList = budgetRepository.getAllByUserUserId(user.getUserId());

        if (startDate != null && endDate != null) {
            transactionList = expenseTransactionRepository
                    .findAllByUserUserIdAndExpenseTransactionDateBetween(
                            user.getUserId(), LocalDate.now().minusDays(30), LocalDate.now());
        } else {
            transactionList = expenseTransactionRepository
                    .findAllByUserUserId(user.getUserId());
        }
        List<ExpenseTransactionResponseDto> responseList = new ArrayList<>();
        BigDecimal totalDebitAmount = BigDecimal.ZERO;
        BigDecimal totalCreditAmount = BigDecimal.ZERO;
        BigDecimal currentBalance;
        BigDecimal totalRemainingBudget = BigDecimal.ZERO;
        // get the total remaining budget :
        if(budgetList != null) {
            for(Budget budget : budgetList) {
                totalRemainingBudget =  totalRemainingBudget.add(budget.getBudgetAmount().subtract(budget.getBudgetSpent()));
            }
        }
        for(ExpenseTransaction transaction : transactionList) {
            responseList.add(
                    new ExpenseTransactionResponseDto(
                    transaction.getExpenseTransactionId(),
                    transaction.getExpenseTransactionType(),
                    transaction.getExpenseAmount(),
                    transaction.getExpenseTransactionDate(),
                    transaction.getExpenseTransactionCategory(),
                    transaction.getExpenseDescription()
            )
            );
            if(transaction.getExpenseTransactionType().equals(ExpenseTransactionType.DEBIT)) {
                totalDebitAmount = totalDebitAmount.add(transaction.getExpenseAmount());
            }else {
                totalCreditAmount = totalCreditAmount.add(transaction.getExpenseAmount());
            }
        }

        currentBalance = totalCreditAmount.subtract(totalDebitAmount);

        return new ExpenseTransactionSummaryDto(
                responseList,
                currentBalance,
                totalDebitAmount,
                totalCreditAmount
        );
    }

    public List<ExpenseTransactionResponseDto> getAllExpensesByType(ExpenseTransactionType type) {
        User user = SecurityUtils.getCurrentUser();
        List<ExpenseTransaction> list = expenseTransactionRepository.findAllByUserUserIdAndExpenseTransactionType(user.getUserId(), type);

        return list.stream()
                .map(transaction -> new ExpenseTransactionResponseDto(
                        transaction.getExpenseTransactionId(),
                        transaction.getExpenseTransactionType(),
                        transaction.getExpenseAmount(),
                        transaction.getExpenseTransactionDate(),
                        transaction.getExpenseTransactionCategory(),
                        transaction.getExpenseDescription()
                )).toList();
    }

    @Transactional
    @CacheEvict(value = "userFinancialContext", key = "@securityUtils.getCurrentUser().getUserId()")
    public ExpenseTransactionResponseDto editExpenseTransaction(Long expenseTransactionId, ExpenseTransactionRequestDto expenseTransactionRequestDto) {
        User user = SecurityUtils.getCurrentUser();

        // fetch the transaction that needs to be edited
        ExpenseTransaction transaction = expenseTransactionRepository.findByExpenseTransactionId(expenseTransactionId).orElseThrow(
                () -> new NotFoundException("Expense transaction with id : " +expenseTransactionId + " not found")
        );
        if(!(user.getUserId().equals(transaction.getUser().getUserId()))) {
            throw new AccessDeniedException("You are not authorized to access this transaction, " +user.getFirstName()
            );
        }
        if(transaction.getExpenseTransactionDate().isBefore(LocalDate.now().minusDays(30))) {
            throw new AccessDeniedException("You are not allowed to edit transactions older than 30 days");
        }

        boolean isCurrentMonth =
                transaction.getExpenseTransactionDate().getMonthValue() == LocalDate.now().getMonthValue() &&
                        transaction.getExpenseTransactionDate().getYear() == LocalDate.now().getYear();

        if (isCurrentMonth) {
            // check if the budget exists cause if the budget doesn't exist, we won't be able to update the budget and the updatebudgetspent will return null pointer exception
            Budget budget = budgetRepository.findByUserUserIdAndBudgetCategory(user.getUserId(), expenseTransactionRequestDto.expenseCategory());
            if(budget != null) {
                // Always reverse old (first of all, reverse the old transaction if it was a debit),
                // cause if the old transaction was a debit, we need to reverse it to change the budget spent,
                // so like if 500 was spent before, we need to add 500 in the budget spent to change the affect
                if (transaction.getExpenseTransactionType() == ExpenseTransactionType.DEBIT) {
                    updateBudgetSpent(
                            user.getUserId(),
                            ExpenseTransactionType.CREDIT,
                            transaction.getExpenseTransactionCategory(),
                            transaction.getExpenseAmount());
                }
                // Always apply new
                // now assume, if the new transaction is a debit of 800, we changed the 500 budget spent above, but now we need to add the new amount to the budget spent:
                if (expenseTransactionRequestDto.expenseTransactionType() == ExpenseTransactionType.DEBIT) {
                    updateBudgetSpent(
                            user.getUserId(),
                            ExpenseTransactionType.DEBIT,
                            expenseTransactionRequestDto.expenseCategory(),
                            expenseTransactionRequestDto.expenseAmount());
                }
            }

        }
        transaction.setExpenseTransactionType(expenseTransactionRequestDto.expenseTransactionType());
        transaction.setExpenseAmount(expenseTransactionRequestDto.expenseAmount());
        transaction.setExpenseTransactionDate(LocalDate.now());
        transaction.setExpenseTransactionCategory(expenseTransactionRequestDto.expenseCategory());
        transaction.setExpenseDescription(expenseTransactionRequestDto.expenseDescription());

        expenseTransactionRepository.save(transaction);

        return new ExpenseTransactionResponseDto(
                transaction.getExpenseTransactionId(),
                transaction.getExpenseTransactionType(),
                transaction.getExpenseAmount(),
                transaction.getExpenseTransactionDate(),
                transaction.getExpenseTransactionCategory(),
                transaction.getExpenseDescription()
        );
    }

    public List<ExpenseTransactionResponseDto> getAllExpenseTransactionByCategory(Category category) {
        User user = SecurityUtils.getCurrentUser();
        List<ExpenseTransaction> list = expenseTransactionRepository.findAllByUserUserIdAndExpenseTransactionCategory(user.getUserId(), category);


        return list.stream()
                .map(transaction -> new ExpenseTransactionResponseDto(
                        transaction.getExpenseTransactionId(),
                        transaction.getExpenseTransactionType(),
                        transaction.getExpenseAmount(),
                        transaction.getExpenseTransactionDate(),
                        transaction.getExpenseTransactionCategory(),
                        transaction.getExpenseDescription()
                )).toList();
    }

    private void updateBudgetSpent(Long userId, ExpenseTransactionType type, Category expenseTransactionCategory, BigDecimal expenseAmount) {
        Budget budget = budgetRepository.findByUserUserIdAndBudgetCategory(userId, expenseTransactionCategory);

        if(budget != null) {
            if(type.equals(ExpenseTransactionType.DEBIT)) {
                budget.setBudgetSpent(budget.getBudgetSpent().add(expenseAmount));
            }else {
                budget.setBudgetSpent(budget.getBudgetSpent().subtract(expenseAmount));
            }
            budgetRepository.save(budget);
        }

        assert budget != null;
        checkAndSendBudgetAlert(SecurityUtils.getCurrentUser(), budget);
    }


    private void checkAndSendBudgetAlert(User user, Budget budget) {
        if(budget.getBudgetAmount().compareTo(BigDecimal.ZERO) == 0) return;

        BigDecimal percentageSpent = budget.getBudgetSpent()
                .divide(budget.getBudgetAmount(), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        String alert = null;

        if(percentageSpent.compareTo(BigDecimal.valueOf(100)) >= 0) {
          alert = String.format("You have exceeded your %s of %f !",budget.getBudgetCategory(), budget.getBudgetAmount());
        }else if(percentageSpent.compareTo(BigDecimal.valueOf(90)) >= 0) {
          alert = String.format("You are close to exceeding your %s of %f !",budget.getBudgetCategory(), budget.getBudgetAmount());
        }else if(percentageSpent.compareTo(BigDecimal.valueOf(80)) >= 0) {
          alert = String.format("You are almost there with your %s of %f !",budget.getBudgetCategory(), budget.getBudgetAmount());
        }

        if(alert != null){
            messagingTemplate.convertAndSendToUser(user.getUserEmail(),"/queue/alerts",alert);
        }
    }



}
