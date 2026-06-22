package com.example.finatebackend.service;

import com.example.finatebackend.dao.BudgetRepository;
import com.example.finatebackend.dao.ExpenseTransactionRepository;
import com.example.finatebackend.dto.ai.ChatRequestDto;
import com.example.finatebackend.dto.ai.ChatResponseDto;
import com.example.finatebackend.dto.ai.WeeklyInsightResponseDto;
import com.example.finatebackend.exceptions.NotFoundException;
import com.example.finatebackend.model.Budget;
import com.example.finatebackend.model.ExpenseTransaction;
import com.example.finatebackend.model.User;
import com.example.finatebackend.security.SecurityUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
public class AiService {

    private final ExpenseTransactionRepository expenseTransactionRepository;
    private final BudgetRepository budgetRepository;
    private final EmailService emailService;
    ChatClient chatClient;
    private final RedisTemplate<String, String> redisTemplate;

    public AiService(ExpenseTransactionRepository expenseTransactionRepository, BudgetRepository budgetRepository, ChatClient.Builder chatClientBuilder, EmailService emailService, RedisTemplate<String, String> redisTemplate) {
        this.expenseTransactionRepository = expenseTransactionRepository;
        this.budgetRepository = budgetRepository;

        this.chatClient =
                chatClientBuilder
                .defaultSystem("You are an expert personal finance advisor. Analyze the user's financial data. Give specific, actionable advice with numbers. Keep it concise in 3-4 short points.")
                .build();

        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
    }

    // called by controller — reads from Redis only, no AI call
    public WeeklyInsightResponseDto getWeeklyInsights() {
        User user = SecurityUtils.getCurrentUser();
        String cacheKey = "weeklyInsight::" + user.getUserId();

        String cachedInsight = redisTemplate.opsForValue().get(cacheKey);
        if (cachedInsight != null) {
            return new WeeklyInsightResponseDto(cachedInsight);
        }

        throw new NotFoundException("No weekly insight available yet. Check back on Monday!");
    }

    // called by scheduler — generates AI response and stores in Redis
    public void generateInsightForUser(User user) {
        LocalDate weekAgo = LocalDate.now().minusWeeks(1);

        List<ExpenseTransaction> expenseTransactionList = expenseTransactionRepository
                .findAllByUserUserIdAndExpenseTransactionDateBetween(
                        user.getUserId(), weekAgo, LocalDate.now());

        List<Budget> budgetList = budgetRepository.findAllByUserUserId(user.getUserId());

        String prompt = buildPrompt(expenseTransactionList, budgetList);

        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        String cacheKey = "weeklyInsight::" + user.getUserId();
        redisTemplate.opsForValue().set(cacheKey, aiResponse, Duration.ofDays(7));

        emailService.sendWeeklyInsightMail(user.getUserEmail(), user.getFirstName(), aiResponse);
    }

    private String buildPrompt(List<ExpenseTransaction> expenseTransactionList, List<Budget> budgetList) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("You are a personal finance advisor. Analyze this user's last 7 days of transactions and give specific, actionable advice in 3-4 short points considering the budget.\n\n");

        promptBuilder.append("TRANSACTIONS:\n");

        for (ExpenseTransaction expenseTransaction : expenseTransactionList) {
            promptBuilder.append(" on ")
                    .append(expenseTransaction.getExpenseTransactionDate())
                    .append(" amount: ")
                    .append(expenseTransaction.getExpenseAmount())
                    .append(" - ")
                    .append(expenseTransaction.getExpenseTransactionType())
                    .append(" on: ")
                    .append(expenseTransaction.getExpenseTransactionCategory())
                    .append(" - ")
                    .append(expenseTransaction.getExpenseDescription() != null ? " - " + expenseTransaction.getExpenseDescription() : "");
        }

        promptBuilder.append("BUDGET:\n");
        for(Budget budget : budgetList) {
            promptBuilder.append(" spent: ")
                    .append(budget.getBudgetSpent())
                    .append(" out of:  ")
                    .append(budget.getBudgetAmount())
                    .append(" on ")
                    .append(budget.getBudgetCategory());
        }
        promptBuilder.append("\nGive practical advice. Be specific with numbers. Keep it concise.");

        return promptBuilder.toString();
    }


    public ChatResponseDto chat(ChatRequestDto request) {
        User user = SecurityUtils.getCurrentUser();

        // Build context same as weekly insight
        String context = buildUserFinancialContext(user.getUserId());

        String response = chatClient.prompt()
                .user(u -> u.text("Context:\n{context}\n\nQuestion: {question}")
                        .param("context", context)
                        .param("question", request.message()))
                .call()
                .content();

        return new ChatResponseDto(response);
    }

    private String buildUserFinancialContext(Long userId) {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        // fetch the transactions from the last 30 days:
        List<ExpenseTransaction> allTransactions = expenseTransactionRepository.findAllByUserUserIdAndExpenseTransactionDateBetween(userId, thirtyDaysAgo, LocalDate.now());
        // fetch the budgets from the last 30 days:
        List<Budget> allBudgets = budgetRepository.findAllByUserUserId(userId);

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("--- USER FINANCIAL DATA CONTEXT ---\n\n");

        contextBuilder.append("CURRENT BUDGETS STATUS:\n");
        if (allBudgets.isEmpty()) {
            contextBuilder.append("No active budgets set up yet.\n");
        } else {
            for (Budget budget : allBudgets) {
                BigDecimal remaining = budget.getBudgetAmount().subtract(budget.getBudgetSpent());
                contextBuilder.append("- Category: ").append(budget.getBudgetCategory())
                        .append(" | Limit: ").append(budget.getBudgetAmount())
                        .append(" | Spent: ").append(budget.getBudgetSpent())
                        .append(" | Remaining: ").append(remaining)
                        .append("\n");
            }
        }

        contextBuilder.append("\nRECENT TRANSACTION HISTORY:\n");
        if (allTransactions.isEmpty()) {
            contextBuilder.append("No recorded transactions found.\n");
        } else {
            for (ExpenseTransaction tx : allTransactions) {
                contextBuilder.append("- Date: ").append(tx.getExpenseTransactionDate())
                        .append(" | Amount: ").append(tx.getExpenseAmount())
                        .append(" | Type: ").append(tx.getExpenseTransactionType())
                        .append(" | Category: ").append(tx.getExpenseTransactionCategory())
                        .append(" | Description: ").append(tx.getExpenseDescription() != null ? tx.getExpenseDescription() : "N/A")
                        .append("\n");
            }
        }

        contextBuilder.append("\n--- END OF CONTEXT ---");
        return contextBuilder.toString();
    }
}
