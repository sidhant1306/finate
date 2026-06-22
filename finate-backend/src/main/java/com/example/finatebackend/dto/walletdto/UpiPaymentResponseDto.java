package com.example.finatebackend.dto.walletdto;

import java.math.BigDecimal;

public record UpiPaymentResponseDto(
        Long transactionId,
        Long receiverId,
        Long senderId,
        BigDecimal amount,
        String receiverName
) {
}
