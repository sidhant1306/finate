package com.example.finatebackend.dto.walletdto;

import com.example.finatebackend.enums.PaymentType;
import com.example.finatebackend.enums.TransactionStatus;
import com.example.finatebackend.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WalletTransactionResponseDto(
        PaymentType paymentType,
        TransactionType transactionType,
        TransactionStatus transactionStatus,
        BigDecimal amount,
        Long recipientId,
        LocalDate transactionDate
) {
}
