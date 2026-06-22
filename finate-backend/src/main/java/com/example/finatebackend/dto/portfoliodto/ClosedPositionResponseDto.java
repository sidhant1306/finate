package com.example.finatebackend.dto.portfoliodto;


import com.example.finatebackend.dto.stockdto.StockHoldingResponseDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClosedPositionResponseDto(
    StockHoldingResponseDto soldStock,
    BigDecimal sellPrice,
    BigDecimal sellValue,
    LocalDate sellDate,
    BigDecimal realizedPnL,
    BigDecimal realizedPnLPercent
) { }
