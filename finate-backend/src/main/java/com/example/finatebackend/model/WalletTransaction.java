package com.example.finatebackend.model;

import com.example.finatebackend.enums.Category;
import com.example.finatebackend.enums.PaymentType;
import com.example.finatebackend.enums.TransactionStatus;
import com.example.finatebackend.enums.TransactionType;
import com.razorpay.Payment;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "wallet_transactions")
public class WalletTransaction {

    @Id
    @GeneratedValue
    private Long walletTransactionId;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @NotNull
    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = true)
    private Long recipientId;

    private LocalDate transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;
}
