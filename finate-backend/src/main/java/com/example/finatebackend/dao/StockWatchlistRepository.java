package com.example.finatebackend.dao;

import com.example.finatebackend.model.StockWatchlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockWatchlistRepository extends JpaRepository<StockWatchlist, Long> {
    List<StockWatchlist> findAllByUserUserId(Long userId);

    Optional<StockWatchlist> findByUserUserIdAndStockSymbol(Long userId, String symbol);

    void removeStockWatchlistByWatchlistId(Long watchlistId);

    boolean existsByUserUserIdAndStockSymbol(Long userId, String s);
}