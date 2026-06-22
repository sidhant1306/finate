package com.example.finatebackend.dto.portfoliodto;

import com.example.finatebackend.dto.stockdto.StockHoldingResponseDto;

import java.math.BigDecimal;

public record ActiveHoldingResponseDto(
    StockHoldingResponseDto stockHolding,
    BigDecimal currentPrice,
    BigDecimal currentValue,    // holding quantity * price
    BigDecimal currentInvestedAmount,       // the buy price * quantity
    BigDecimal unrealizedPnL,
    BigDecimal unrealizedPnLPercent
) {
}
