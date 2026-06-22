package com.example.finatebackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stock_holding")

public class StockHolding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private String companyName;
    private BigDecimal quantity;


    private BigDecimal buyPrice;

    private BigDecimal sellPrice;

    private BigDecimal sellValue;

    private LocalDate sellDate;

    private LocalDate buyDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}