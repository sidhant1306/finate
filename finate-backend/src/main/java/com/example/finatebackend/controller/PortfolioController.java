package com.example.finatebackend.controller;

import com.example.finatebackend.dto.portfoliodto.PortfolioResponseDto;
import com.example.finatebackend.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/get-portfolio")
    public ResponseEntity<PortfolioResponseDto> getPortfolio() {
        return ResponseEntity.ok(portfolioService.getPortfolio());
    }


}
