package com.example.finatebackend.dto.walletdto;

import java.math.BigDecimal;

public record PaymentResponseDto(
        String orderId,
        BigDecimal amount,
        String currency,
        String keyId
) {
}
