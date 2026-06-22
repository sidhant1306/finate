package com.example.finatebackend.service;

import com.example.finatebackend.dao.ExpenseTransactionRepository;
import com.example.finatebackend.dao.UserRepository;
import com.example.finatebackend.dao.WalletRepository;
import com.example.finatebackend.dto.dashboarddto.DashboardResponseDto;
import com.example.finatebackend.dto.expensedto.ExpenseTransactionSummaryDto;
import com.example.finatebackend.enums.ExpenseTransactionType;
import com.example.finatebackend.helper.HelperFunctions;
import com.example.finatebackend.model.ExpenseTransaction;
import com.example.finatebackend.model.User;
import com.example.finatebackend.model.Wallet;
import com.example.finatebackend.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {
    private final TrackingService trackingService;
    private final ExpenseTransactionRepository expenseTransactionRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final HelperFunctions helperFunctions;

    public DashboardService(TrackingService trackingService, ExpenseTransactionRepository expenseTransactionRepository, UserRepository userRepository, WalletRepository walletRepository, HelperFunctions helperFunctions) {
        this.trackingService = trackingService;
        this.expenseTransactionRepository = expenseTransactionRepository;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.helperFunctions = helperFunctions;
    }

    public DashboardResponseDto getDashboardInfo() {
        User user = SecurityUtils.getCurrentUser();
        Wallet wallet = walletRepository.findByWalletUser(user).orElse(null);
        ExpenseTransactionSummaryDto summaryDto = trackingService.getAllExpenses(LocalDate.now().minusDays(30), LocalDate.now());
        BigDecimal netWorth = getNetWorth(user.getUserId());
        assert wallet != null;
        return new DashboardResponseDto(
                 wallet.getCurrentBalance(),
                 netWorth,
                 summaryDto.currentBalance(),
                 summaryDto.totalDebitAmount(),
                 summaryDto.totalCreditAmount(),
                 summaryDto.expenseTransactionResponseDtoList()
         );
    }

    private BigDecimal getNetWorth(Long userId) {
        List<ExpenseTransaction> allTransactions =
                expenseTransactionRepository.findAllByUserUserId(userId);

        BigDecimal totalCredit = helperFunctions.getTotalCredit(allTransactions);

        BigDecimal totalDebit = helperFunctions.getTotalDebit(allTransactions);

        return totalCredit.subtract(totalDebit);
    }
}
