package com.example.finatebackend.controller;

import com.example.finatebackend.dto.finnhubdto.FinnhubQuoteResponseDto;
import com.example.finatebackend.dto.finnhubdto.StockSearchResultDto;
import com.example.finatebackend.service.FinnhubService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/finnhub")
public class FinnhubController {

    private final FinnhubService finnhubService;

    public FinnhubController(FinnhubService finnhubService) {
        this.finnhubService = finnhubService;
    }

    @GetMapping("/quote/{symbol}")
    public ResponseEntity<FinnhubQuoteResponseDto> getStockQuote(@PathVariable String symbol) {
        return ResponseEntity.ok(finnhubService.getQuote(symbol));
    }

    @GetMapping("/search/{query}")
    public ResponseEntity<List<StockSearchResultDto>> getStockSearch(@PathVariable String query) {
        return ResponseEntity.ok(finnhubService.searchStocks(query));
    }

}
