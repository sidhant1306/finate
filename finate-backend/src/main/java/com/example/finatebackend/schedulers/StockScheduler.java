package com.example.finatebackend.schedulers;

import com.example.finatebackend.dao.StockHoldingRepository;
import com.example.finatebackend.dao.StockWatchlistRepository;
import com.example.finatebackend.dto.finnhubdto.FinnhubQuoteResponseDto;
import com.example.finatebackend.service.FinnhubService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@EnableScheduling
public class StockScheduler {
    private final StockWatchlistRepository stockWatchlistRepository;
    private final StockHoldingRepository stockHoldingRepository;
    private final FinnhubService finnhubService;
    private final SimpMessagingTemplate messagingTemplate;

    public StockScheduler(StockWatchlistRepository stockWatchlistRepository, StockHoldingRepository stockHoldingRepository, FinnhubService finnhubService, SimpMessagingTemplate messagingTemplate) {
        this.stockWatchlistRepository = stockWatchlistRepository;
        this.stockHoldingRepository = stockHoldingRepository;
        this.finnhubService = finnhubService;
        this.messagingTemplate = messagingTemplate;
    }


    @Scheduled(fixedRate = 30000)
    public void broadcastStockPrices() {
        // we need to broadcast stock prices of all the clients using finate
        // so fetch all the symbols from every user :

        Set<String> uniqueSymbols = new HashSet<>();

        stockWatchlistRepository.findAll()
                .forEach(stockWatchlist -> uniqueSymbols.add(stockWatchlist.getStockSymbol()));

        // now find the symbols of stock holdings of every user :

        stockHoldingRepository.findAllBySellPriceIsNull()
                .forEach(stockHolding -> uniqueSymbols.add(stockHolding.getSymbol()));

        if(uniqueSymbols.isEmpty()) return;

        // now, as we have all the unique symbols, get the quote for each one of them:

        List<FinnhubQuoteResponseDto> currentPrices = new ArrayList<>();

        try {
            currentPrices = uniqueSymbols.stream()
                    .map(finnhubService::getQuote)
                    .toList();
        }catch (Exception e) {
            e.printStackTrace();
        }

        // now we have the quotes for all the symbols
        messagingTemplate.convertAndSend("/topic/stock-prices", currentPrices);

    }

}
