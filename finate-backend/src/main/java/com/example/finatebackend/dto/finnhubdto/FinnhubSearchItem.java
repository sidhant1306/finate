package com.example.finatebackend.dto.finnhubdto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FinnhubSearchItem(
        // to get the individual item details :
        String symbol,
        String description // this stores the company name
) {
}
