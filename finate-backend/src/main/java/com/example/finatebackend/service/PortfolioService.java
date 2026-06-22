package com.example.finatebackend.service;

import com.example.finatebackend.dao.StockHoldingRepository;
import com.example.finatebackend.dto.finnhubdto.FinnhubQuoteResponseDto;
import com.example.finatebackend.dto.portfoliodto.ActiveHoldingResponseDto;
import com.example.finatebackend.dto.portfoliodto.ClosedPositionResponseDto;
import com.example.finatebackend.dto.portfoliodto.PortfolioResponseDto;
import com.example.finatebackend.dto.stockdto.StockHoldingResponseDto;
import com.example.finatebackend.exceptions.NotFoundException;
import com.example.finatebackend.model.StockHolding;
import com.example.finatebackend.model.User;
import com.example.finatebackend.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class PortfolioService {


    private final StockHoldingRepository stockHoldingRepository;
    private final FinnhubService finnhubService;

    public PortfolioService(StockHoldingRepository stockHoldingRepository, FinnhubService finnhubService) {
        this.stockHoldingRepository = stockHoldingRepository;
        this.finnhubService = finnhubService;
    }

    public PortfolioResponseDto getPortfolio() {
        User user = SecurityUtils.getCurrentUser();

        List<ActiveHoldingResponseDto> activeHoldingResponseDto = getActiveHolding(user.getUserId());
        List<ClosedPositionResponseDto> closedPositionResponseDto = getClosedPosition(user.getUserId());

        System.out.println("active holdings while selling : " +activeHoldingResponseDto);

        BigDecimal totalCurrentAmountInvested = BigDecimal.ZERO;
         BigDecimal currentValue = BigDecimal.ZERO;
         BigDecimal lifetimeInvested;
         BigDecimal unrealizedPnL = BigDecimal.ZERO;
         BigDecimal realizedPnL = BigDecimal.ZERO;
         BigDecimal totalPnL;
         BigDecimal totalSoldStockAmountInvested = BigDecimal.ZERO;

        for(ActiveHoldingResponseDto active : activeHoldingResponseDto) {
            totalCurrentAmountInvested = totalCurrentAmountInvested.add(active.currentInvestedAmount());
            currentValue = currentValue.add(active.currentValue());
            unrealizedPnL = unrealizedPnL.add(active.unrealizedPnL());
        }

        for(ClosedPositionResponseDto closed : closedPositionResponseDto) {
            totalSoldStockAmountInvested = totalSoldStockAmountInvested.add(closed.soldStock().buyPrice());
            realizedPnL = realizedPnL.add(closed.realizedPnL());
        }
        lifetimeInvested = totalCurrentAmountInvested.add(totalSoldStockAmountInvested);
        totalPnL = unrealizedPnL.add(realizedPnL);

        return new PortfolioResponseDto(
                activeHoldingResponseDto,
                closedPositionResponseDto,
                totalCurrentAmountInvested,
                currentValue,
                lifetimeInvested,
                unrealizedPnL,
                realizedPnL,
                totalPnL
        );

    }

    // get the active holdings :

    private List<ActiveHoldingResponseDto> getActiveHolding(Long userId) {
        List<StockHolding> stockHolding = stockHoldingRepository
                .findAllStockHoldingByUserUserIdAndQuantityGreaterThan(userId, BigDecimal.ZERO)
                .orElse(new ArrayList<>());
        List<ActiveHoldingResponseDto> activeHoldingResponseDto = new ArrayList<>();

        for (StockHolding holding : stockHolding) {
            BigDecimal currentPrice = BigDecimal.valueOf(finnhubService.getQuote(holding.getSymbol()).c());
            BigDecimal currentValue = currentPrice.multiply(holding.getQuantity());
            BigDecimal currentInvestedAmount = holding.getBuyPrice().multiply(holding.getQuantity());
            BigDecimal unrealizedPnL = currentValue.subtract(currentInvestedAmount);

            BigDecimal unrealizedPnLPercent = currentInvestedAmount.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : unrealizedPnL.divide(currentInvestedAmount, 2, RoundingMode.HALF_DOWN)
                    .multiply(BigDecimal.valueOf(100));

            StockHoldingResponseDto responseDto = new StockHoldingResponseDto(
                    holding.getId(),
                    holding.getSymbol(),
                    holding.getQuantity(),
                    holding.getCompanyName(),
                    holding.getBuyPrice(),
                    holding.getBuyDate()
            );

            activeHoldingResponseDto.add(new ActiveHoldingResponseDto(
                    responseDto,
                    currentPrice,
                    currentValue,
                    currentInvestedAmount,
                    unrealizedPnL,
                    unrealizedPnLPercent
            ));
        }
        return activeHoldingResponseDto;
    }



    private List<ClosedPositionResponseDto> getClosedPosition(Long userId) {
        List<StockHolding> stockHolding = stockHoldingRepository
                .findAllStockHoldingByUserUserIdAndQuantityEquals(userId, BigDecimal.ZERO)
                .orElse(new ArrayList<>());

        List<ClosedPositionResponseDto> closedPositionResponseDto = new ArrayList<>();

        for (StockHolding holding : stockHolding) {
            // recover the original sold quantity from sellValue / sellPrice
            BigDecimal soldQuantity = holding.getSellValue()
                    .divide(holding.getSellPrice(), 6, RoundingMode.HALF_DOWN);

            BigDecimal totalInvested = holding.getBuyPrice().multiply(soldQuantity);

            BigDecimal realizedPnL = holding.getSellValue().subtract(totalInvested);

            BigDecimal realizedPnLPercent = totalInvested.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : realizedPnL.divide(totalInvested, 2, RoundingMode.HALF_DOWN)
                    .multiply(BigDecimal.valueOf(100));

            StockHoldingResponseDto stockHoldingResponseDto = new StockHoldingResponseDto(
                    holding.getId(),
                    holding.getSymbol(),
                    soldQuantity,
                    holding.getCompanyName(),
                    holding.getBuyPrice(),
                    holding.getBuyDate()
            );

            closedPositionResponseDto.add(new ClosedPositionResponseDto(
                    stockHoldingResponseDto,
                    holding.getSellPrice(),
                    holding.getSellValue(),
                    holding.getSellDate(),
                    realizedPnL,
                    realizedPnLPercent
            ));
        }
        return closedPositionResponseDto;
    }
}
