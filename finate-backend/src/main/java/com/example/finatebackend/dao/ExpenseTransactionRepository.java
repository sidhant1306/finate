package com.example.finatebackend.dao;

import com.example.finatebackend.enums.Category;
import com.example.finatebackend.enums.ExpenseTransactionType;
import com.example.finatebackend.model.ExpenseTransaction;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseTransactionRepository extends JpaRepository<ExpenseTransaction, Long> {
    Optional<ExpenseTransaction> findByExpenseTransactionId(Long expenseTransactionId);

    List<ExpenseTransaction> findAllByUserUserId(Long userId);

    List<ExpenseTransaction> findAllByUserUserIdAndExpenseTransactionCategory(Long userId, Category category);

    List<ExpenseTransaction> findAllByUserUserIdAndExpenseTransactionType(Long userId, ExpenseTransactionType type);

    List<ExpenseTransaction> findAllByUserUserIdAndExpenseTransactionDateBetween(Long userId, LocalDate startDate, LocalDate endDate);


    List<ExpenseTransaction> findAllByUserUserIdAndExpenseTransactionCategoryAndExpenseTransactionDateBetweenAndExpenseTransactionType(Long userId, @NotBlank Category category, LocalDate localDate, LocalDate now, ExpenseTransactionType expenseTransactionType);
}
