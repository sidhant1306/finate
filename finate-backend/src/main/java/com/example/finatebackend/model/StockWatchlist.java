package com.example.finatebackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "stock_watchlist")
public class StockWatchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long watchlistId;

    private String stockSymbol;

    private String stockCompanyName;

    private BigDecimal priceWhenAdded;

    private LocalDate watchlistDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
