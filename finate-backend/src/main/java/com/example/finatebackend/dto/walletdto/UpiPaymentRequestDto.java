package com.example.finatebackend.dto.walletdto;

import java.math.BigDecimal;

public record UpiPaymentRequestDto(
        Long receiverUserid,
        BigDecimal amount
) {
}
