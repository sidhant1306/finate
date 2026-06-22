package com.example.finatebackend.dto.stockdto;

import java.math.BigDecimal;

public record StockHoldingRequestDto(
        String symbol,
        String companyName,
        BigDecimal quantity
) {
}
