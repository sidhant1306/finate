package com.example.finatebackend.helper;

import com.example.finatebackend.enums.ExpenseTransactionType;
import com.example.finatebackend.model.ExpenseTransaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class HelperFunctions {

    public BigDecimal getTotalCredit(List<ExpenseTransaction> transactions) {
       return  transactions.stream()
                .filter(t -> t.getExpenseTransactionType() == ExpenseTransactionType.CREDIT)
                .map(ExpenseTransaction::getExpenseAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalDebit(List<ExpenseTransaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getExpenseTransactionType() == ExpenseTransactionType.DEBIT)
                .map(ExpenseTransaction::getExpenseAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
