package com.example.finatebackend.model;

import com.example.finatebackend.enums.Category;
import com.example.finatebackend.enums.ExpenseTransactionType;
import com.example.finatebackend.enums.TransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "expense_transaction")
public class ExpenseTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long expenseTransactionId;

    @Enumerated(EnumType.STRING)
    private ExpenseTransactionType expenseTransactionType;

    @NotNull
    @Column(nullable = false)
    private BigDecimal expenseAmount;

    private LocalDate expenseTransactionDate;

    @Enumerated(EnumType.STRING)
    private Category expenseTransactionCategory;

    @Column(nullable = true)
    private String expenseDescription;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
