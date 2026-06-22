package com.example.finatebackend.controller;

import com.example.finatebackend.dao.ExpenseTransactionRepository;
import com.example.finatebackend.dto.expensedto.ExpenseTransactionRequestDto;
import com.example.finatebackend.dto.expensedto.ExpenseTransactionResponseDto;
import com.example.finatebackend.dto.expensedto.ExpenseTransactionSummaryDto;
import com.example.finatebackend.enums.Category;
import com.example.finatebackend.enums.ExpenseTransactionType;
import com.example.finatebackend.model.ExpenseTransaction;
import com.example.finatebackend.model.User;
import com.example.finatebackend.security.SecurityUtils;
import com.example.finatebackend.service.EmailService;
import com.example.finatebackend.service.TrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    private final TrackingService trackingService;
    private final EmailService emailService;
    private final ExpenseTransactionRepository expenseTransactionRepository;

    public TrackingController(TrackingService trackingService, EmailService emailService, ExpenseTransactionRepository expenseTransactionRepository) {
        this.trackingService = trackingService;
        this.emailService = emailService;
        this.expenseTransactionRepository = expenseTransactionRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<ExpenseTransactionResponseDto> createExpenseTransaction(@RequestBody ExpenseTransactionRequestDto expenseTransactionRequestDto) {
        return ResponseEntity.ok(trackingService.createExpenseTransaction(expenseTransactionRequestDto));
    }

    @DeleteMapping("/delete-expense-transaction-by-id")
    public ResponseEntity<String> deleteExpenseTransactionById(@RequestParam Long expenseTransactionId) {
        return ResponseEntity.ok(trackingService.deleteExpenseTransactionById(expenseTransactionId));
    }

    @GetMapping("/get-all-expenses")
    public ResponseEntity<ExpenseTransactionSummaryDto> getAllExpenses(@RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate){
        return ResponseEntity.ok(trackingService.getAllExpenses(startDate,endDate));
    }

    @GetMapping("/get-all-expenses-by-type")
    public ResponseEntity<List<ExpenseTransactionResponseDto>> getAllExpensesByType(@RequestParam ExpenseTransactionType expenseTransactionType) {
        return ResponseEntity.ok(trackingService.getAllExpensesByType(expenseTransactionType));
    }

    @PutMapping("/edit-expense-transaction-by-id")
    public ResponseEntity<ExpenseTransactionResponseDto> editExpenseTransaction(@RequestParam Long expenseTransactionId, @RequestBody ExpenseTransactionRequestDto expenseTransactionRequestDto) {
        return ResponseEntity.ok(trackingService.editExpenseTransaction(expenseTransactionId, expenseTransactionRequestDto));
    }

    @GetMapping("/get-all-expenses-by-category")
    public ResponseEntity<List<ExpenseTransactionResponseDto>> getAllExpenseTransactionByCategory(@RequestParam Category category) {
        return ResponseEntity.ok(trackingService.getAllExpenseTransactionByCategory(category));
    }

    @PostMapping("/email-transaction-history")
    public ResponseEntity<String> emailTransactionHistory() {
        User user = SecurityUtils.getCurrentUser();
        LocalDate monthAgo = LocalDate.now().minusDays(30);
        List<ExpenseTransaction> transactionList = expenseTransactionRepository
                .findAllByUserUserIdAndExpenseTransactionDateBetween(user.getUserId(), monthAgo, LocalDate.now());
        emailService.sendMonthlyWalletTransactionsHistory(user.getUserEmail(), user.getFirstName(), transactionList);
        return ResponseEntity.ok("Transaction history sent to " + user.getUserEmail());
    }
}
