package com.example.finatebackend.dto.walletdto;

import com.example.finatebackend.enums.PaymentType;

import java.math.BigDecimal;

public record PaymentRequestDto(
        BigDecimal paymentAmount,
        PaymentType paymentType
    )
{}
