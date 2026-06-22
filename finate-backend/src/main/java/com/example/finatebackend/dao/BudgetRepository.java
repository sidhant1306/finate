package com.example.finatebackend.dao;

import com.example.finatebackend.enums.Category;
import com.example.finatebackend.model.Budget;
import com.example.finatebackend.model.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findAllByUserUserId(Long userId);

    Budget findByUserUserIdAndBudgetCategory(Long userId, @NotBlank Category category);

    Budget findByUserUserIdAndBudgetId(Long userUserId, Long budgetId);

    Long user(User user);

    List<Budget> getAllByUserUserId(Long userId);

}