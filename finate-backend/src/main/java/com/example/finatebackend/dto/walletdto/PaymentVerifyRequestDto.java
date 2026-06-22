package com.example.finatebackend.dto.walletdto;

import com.example.finatebackend.enums.PaymentType;

import java.math.BigDecimal;

public record PaymentVerifyRequestDto(
        String paymentId,

        String orderId,

        String signature,

        PaymentType paymentType,

        BigDecimal amount
) {
}
