package com.example.finatebackend.dto.finnhubdto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FinnhubSearchResponseDto(
        // finnhub always returns a list of symbols and details(for different regions):
        List<FinnhubSearchItem> result
)
{}
