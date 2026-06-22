package com.example.finatebackend.service;

import com.example.finatebackend.dto.finnhubdto.FinnhubQuoteResponseDto;
import com.example.finatebackend.dto.finnhubdto.FinnhubSearchResponseDto;
import com.example.finatebackend.dto.finnhubdto.StockSearchResultDto;
import com.example.finatebackend.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class FinnhubService {

    @Value("${finnhub.api.key}")
    private String finnhubApiKey;

    @Value("${finnhub.api.base-url}")
    private String finnhubBaseUrl;

    private final RestTemplate restTemplate;

    public FinnhubService(RestTemplate template) {
        this.restTemplate = template;
    }

    public List<StockSearchResultDto> searchStocks(String keyword) {
        String url = finnhubBaseUrl + "/search?q=" + keyword + "&token=" + finnhubApiKey;
        FinnhubSearchResponseDto responseDto = restTemplate.getForObject(url, FinnhubSearchResponseDto.class);
        if(responseDto == null || responseDto.result() == null) {
            return List.of();
        }
        System.out.println(responseDto);
        return responseDto.result().stream()
                .filter(item -> !item.symbol().contains(".")) // exclude non us stocks
                .map(item -> new StockSearchResultDto(item.symbol(), item.description()))
                .limit(10)
                .toList();
    }
    @Cacheable(value = "stockPrices", key = "#symbol")
    public FinnhubQuoteResponseDto getQuote(String symbol) {
        String url = finnhubBaseUrl + "/quote?symbol=" + symbol + "&token=" + finnhubApiKey;

        FinnhubQuoteResponseDto quote = restTemplate.getForObject(url, FinnhubQuoteResponseDto.class);
        if(quote == null || quote.c() == 0) {
            throw new NotFoundException("Stock quote not found for symbol: " + symbol);
        }

        return new FinnhubQuoteResponseDto(
                quote.c(),
                quote.h(),
                quote.l(),
                quote.o(),
                quote.pc(),
                quote.t()
        );
    }



}
