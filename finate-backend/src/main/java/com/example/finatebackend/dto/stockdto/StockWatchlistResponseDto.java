package com.example.finatebackend.dto.stockdto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockWatchlistResponseDto(
        String stockSymbol,

        String stockCompanyName,

        BigDecimal priceWhenAdded,

        LocalDate watchlistDate

) {
}
