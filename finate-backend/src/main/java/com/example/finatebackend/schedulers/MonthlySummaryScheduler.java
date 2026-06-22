package com.example.finatebackend.schedulers;

import com.example.finatebackend.dao.ExpenseTransactionRepository;
import com.example.finatebackend.dao.UserRepository;
import com.example.finatebackend.helper.HelperFunctions;
import com.example.finatebackend.model.ExpenseTransaction;
import com.example.finatebackend.model.User;
import com.example.finatebackend.service.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class MonthlySummaryScheduler {


    private final UserRepository userRepository;
    private final ExpenseTransactionRepository expenseTransactionRepository;
    private final EmailService emailService;
    private final HelperFunctions helperFunctions;

    public MonthlySummaryScheduler(UserRepository userRepository, ExpenseTransactionRepository expenseTransactionRepository, EmailService emailService, HelperFunctions helperFunctions) {
        this.userRepository = userRepository;
        this.expenseTransactionRepository = expenseTransactionRepository;
        this.emailService = emailService;
        this.helperFunctions = helperFunctions;
    }

    @Scheduled(cron = "0 0 9 1 * *")
    public void sendMonthlySummaries() {
        List<User> userList = userRepository.findAll();

        LocalDate monthAgo = LocalDate.now().minusDays(30);

        // now send the mail to every user :
        try {
            for (User user : userList) {
                // for each user :

                // get the transaction list :

                List<ExpenseTransaction> expenseTransactionList =
                        expenseTransactionRepository.findAllByUserUserIdAndExpenseTransactionDateBetween(
                                user.getUserId(), monthAgo, LocalDate.now()
                        );

                BigDecimal totalIncome = helperFunctions.getTotalCredit(expenseTransactionList);

                BigDecimal totalExpense = helperFunctions.getTotalDebit(expenseTransactionList);

                BigDecimal netSavings = totalIncome.subtract(totalExpense);

                emailService.sendMonthlySummaryEmail(user.getUserEmail(), user.getFirstName(), totalIncome, totalExpense, netSavings);
            }
        }catch (Exception e) {
            e.getStackTrace();
        }
    }

    @Scheduled(cron = "0 0 9 1 * *")
    public void sendMonthlyTransactions() {
        List<User> usersList = userRepository.findAll();
        LocalDate monthAgo =  LocalDate.now().minusDays(30);
        try {
            for(User user : usersList) {
                List<ExpenseTransaction> transactionList = expenseTransactionRepository.findAllByUserUserIdAndExpenseTransactionDateBetween(user.getUserId(), monthAgo, LocalDate.now());

                emailService.sendMonthlyWalletTransactionsHistory(user.getUserEmail(), user.getFirstName(), transactionList);
            }
        }catch (Exception e) {
            e.getStackTrace();
        }

    }
}
