package com.example.finatebackend.controller;

import com.example.finatebackend.dao.StockWatchlistRepository;
import com.example.finatebackend.dto.stockdto.*;
import com.example.finatebackend.service.StockHoldingService;
import com.example.finatebackend.service.StockWatchlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockHoldingService stockHoldingService;
    private final StockWatchlistRepository stockWatchlistRepository;
    private final StockWatchlistService stockWatchlistService;

    public StockController(StockHoldingService stockHoldingService, StockWatchlistRepository stockWatchlistRepository, StockWatchlistService stockWatchlistService) {
        this.stockHoldingService = stockHoldingService;
        this.stockWatchlistRepository = stockWatchlistRepository;
        this.stockWatchlistService = stockWatchlistService;
    }

    @PostMapping("/buy-stock")
    public ResponseEntity<StockHoldingResponseDto> buyStock(@RequestBody StockHoldingRequestDto stockHoldingRequestDto) {
        return ResponseEntity.ok(stockHoldingService.buyStock(stockHoldingRequestDto));
    }

    @PostMapping("/sell-stock")
    public ResponseEntity<String>  sellStock(@RequestBody StockHoldingRequestDto stockHoldingRequestDto) {
        return ResponseEntity.ok(stockHoldingService.sellStock(stockHoldingRequestDto));
    }

    @PostMapping("/add-stock-to-watchlist")
    public ResponseEntity<StockWatchlistResponseDto> addStockToWatchlist(@RequestBody StockWatchlistRequestDto stockWatchlistRequestDto) {
        return ResponseEntity.ok(stockWatchlistService.addToWatchlist(stockWatchlistRequestDto));
    }

    @GetMapping("/get-entire-watchlist")
    public ResponseEntity<List<StockWatchlistResponseDto>> getAllWatchlist() {
        return ResponseEntity.ok(stockWatchlistService.getAllWatchlist());
    }

    @DeleteMapping("/remove-watchlist")
    public ResponseEntity<String> removeWatchlist(@RequestBody RemoveFromWatchlistRequestDto requestDto) {
        return ResponseEntity.ok(stockWatchlistService.removeFromWatchlist(requestDto));
    }
}
