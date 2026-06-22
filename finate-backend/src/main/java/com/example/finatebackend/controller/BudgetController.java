package com.example.finatebackend.controller;

import com.example.finatebackend.dto.budgetdto.BudgetRequestDto;
import com.example.finatebackend.dto.budgetdto.BudgetResponseDto;
import com.example.finatebackend.dto.budgetdto.BudgetSummaryResponseDto;
import com.example.finatebackend.enums.Category;
import com.example.finatebackend.service.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping("/create")
    public ResponseEntity<BudgetResponseDto> createBudget(@RequestBody BudgetRequestDto budgetRequestDto) {
        return ResponseEntity.ok(budgetService.createBudget(budgetRequestDto));
    }

    @GetMapping("/get-all-budgets")
    public ResponseEntity<BudgetSummaryResponseDto> getAllBudgets() {
        return ResponseEntity.ok(budgetService.getAllBudgets());
    }

    @GetMapping("/get-budget-by-category")
    public ResponseEntity<BudgetResponseDto> getBudgetByCategory(@RequestParam Category category) {
        return ResponseEntity.ok(budgetService.getBudgetByCategory(category));
    }

    @PutMapping("/edit-budget-by-id")
    public ResponseEntity<BudgetResponseDto> editBudget(@RequestParam Long budgetId, @RequestBody BudgetRequestDto budgetRequestDto) {
        return ResponseEntity.ok(budgetService.editBudget(budgetId, budgetRequestDto));
    }

    @DeleteMapping("/delete-budget-by-id")
    public ResponseEntity<String> deleteBudget(@RequestParam Long budgetId) {
        return ResponseEntity.ok(budgetService.deleteBudget(budgetId));
    }

    @GetMapping("/get-budget-by-id")
    public ResponseEntity<BudgetResponseDto> getBudgetById(@RequestParam Long budgetId) {
        return ResponseEntity.ok(budgetService.getBudgetById(budgetId));
    }

    @DeleteMapping("/delete-all-budgets")
    public ResponseEntity<String> deleteAllBudgets() {
        return ResponseEntity.ok(budgetService.deleteAllBudgets());
    }

}

