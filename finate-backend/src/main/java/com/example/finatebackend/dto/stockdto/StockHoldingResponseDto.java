package com.example.finatebackend.dto.stockdto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockHoldingResponseDto(
        Long holdingId,
        String symbol,
        BigDecimal quantity,
        String companyName,
        BigDecimal buyPrice,
        LocalDate buyDate
) {
}
