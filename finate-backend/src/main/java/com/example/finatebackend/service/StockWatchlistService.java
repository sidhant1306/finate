package com.example.finatebackend.service;

import com.example.finatebackend.dao.StockWatchlistRepository;
import com.example.finatebackend.dto.finnhubdto.FinnhubQuoteResponseDto;
import com.example.finatebackend.dto.stockdto.RemoveFromWatchlistRequestDto;
import com.example.finatebackend.dto.stockdto.StockWatchlistRequestDto;
import com.example.finatebackend.dto.stockdto.StockWatchlistResponseDto;
import com.example.finatebackend.exceptions.DuplicateException;
import com.example.finatebackend.exceptions.NotFoundException;
import com.example.finatebackend.model.StockWatchlist;
import com.example.finatebackend.model.User;
import com.example.finatebackend.security.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class StockWatchlistService {

    private final FinnhubService finnhubService;
    private final StockWatchlistRepository stockWatchlistRepository;

    public StockWatchlistService(FinnhubService finnhubService, StockWatchlistRepository stockWatchlistRepository) {
        this.finnhubService = finnhubService;
        this.stockWatchlistRepository = stockWatchlistRepository;
    }

    @Transactional
    public StockWatchlistResponseDto addToWatchlist(StockWatchlistRequestDto stockWatchlistRequestDto) {
        User user = SecurityUtils.getCurrentUser();

        // Check if already in watchlist
        boolean exists = stockWatchlistRepository
                .existsByUserUserIdAndStockSymbol(user.getUserId(), stockWatchlistRequestDto.stockSymbol());

        if (exists) {
            throw new DuplicateException(stockWatchlistRequestDto.stockSymbol() + " already in watchlist");
        }

        FinnhubQuoteResponseDto stock = finnhubService.getQuote(stockWatchlistRequestDto.stockSymbol());

        if(stock.c() == 0) {
            throw new NotFoundException(String.format("Stock with symbol %s not found", stockWatchlistRequestDto.stockSymbol()));
        }
        StockWatchlist watchlist = StockWatchlist
                .builder()
                .stockSymbol(stockWatchlistRequestDto.stockSymbol())
                .stockCompanyName(stockWatchlistRequestDto.CompanyName())
                .priceWhenAdded(BigDecimal.valueOf(stock.c()))
                .watchlistDate(LocalDate.now())
                .user(user)
                .build();

        stockWatchlistRepository.save(watchlist);


        return new StockWatchlistResponseDto(
                watchlist.getStockSymbol(),
                watchlist.getStockCompanyName(),
                watchlist.getPriceWhenAdded(),
                watchlist.getWatchlistDate()
        );
    }

    public List<StockWatchlistResponseDto> getAllWatchlist() {
        User user = SecurityUtils.getCurrentUser();

        List<StockWatchlist> userStockWatchlist = stockWatchlistRepository.findAllByUserUserId(user.getUserId());

        return userStockWatchlist.stream()
                .map(watchlist ->
                        new StockWatchlistResponseDto(
                                watchlist.getStockSymbol(),
                                watchlist.getStockCompanyName(),
                                watchlist.getPriceWhenAdded(),
                                watchlist.getWatchlistDate()
                        )).toList();
    }
    @Transactional
    public String removeFromWatchlist(RemoveFromWatchlistRequestDto removeFromWatchlistRequestDto) {
        User user = SecurityUtils.getCurrentUser();

        StockWatchlist watchlist = stockWatchlistRepository.findByUserUserIdAndStockSymbol(user.getUserId(), removeFromWatchlistRequestDto.stockSymbol()).orElseThrow(
                () -> new NotFoundException("Watchlist does not exist")
        );

        stockWatchlistRepository.removeStockWatchlistByWatchlistId(watchlist.getWatchlistId());

        return String.format("Watchlist with stock symbol %s removed", removeFromWatchlistRequestDto.stockSymbol());
    }
}
