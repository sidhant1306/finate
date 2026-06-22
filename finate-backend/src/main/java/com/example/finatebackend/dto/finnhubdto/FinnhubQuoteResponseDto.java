package com.example.finatebackend.dto.finnhubdto;


public record FinnhubQuoteResponseDto(
        double c, // current price
        double h,   // highest of the day
        double l,   // lowest of the day
        double o,   // open price
        double pc, // previous close price
        long t  // timestamp
) {}
