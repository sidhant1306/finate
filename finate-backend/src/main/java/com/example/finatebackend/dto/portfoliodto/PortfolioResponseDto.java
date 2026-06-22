package com.example.finatebackend.dto.portfoliodto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioResponseDto(
    List<ActiveHoldingResponseDto> activeHoldings,
    List<ClosedPositionResponseDto> closedHoldings,
    BigDecimal totalCurrentAmountInvested,
    BigDecimal CurrentValue,
    BigDecimal lifetimeInvested,
    BigDecimal unrealizedPnL,   // active profit or loss
    BigDecimal realizedPnL,     // booked profit or loss(after selling)
    BigDecimal totalPnL
)
{ }
