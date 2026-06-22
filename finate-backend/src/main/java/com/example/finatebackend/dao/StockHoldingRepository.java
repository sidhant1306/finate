package com.example.finatebackend.dao;

import com.example.finatebackend.model.StockHolding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {
    Optional<StockHolding> findStockHoldingBySymbol(String symbol);

    void removeStockHoldingById(Long id);

    Optional<List<StockHolding>> findAllStockHoldingByUserUserId(Long userId);


    Optional<List<StockHolding>> findAllStockHoldingByUserUserIdAndQuantityGreaterThan(Long userId, BigDecimal quantity);

    Optional<List<StockHolding>> findAllStockHoldingByUserUserIdAndQuantityEquals(Long userUserId, BigDecimal quantity);

    List<StockHolding> findAllBySellPriceIsNull();

    StockHolding findAllStockHoldingByUserUserIdAndQuantityGreaterThanAndSymbol(Long userId, BigDecimal zero, String symbol);

    Optional<StockHolding> findStockHoldingBySymbolAndUserUserId(String symbol, Long userId);
}