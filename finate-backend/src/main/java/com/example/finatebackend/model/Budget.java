package com.example.finatebackend.model;

import com.example.finatebackend.enums.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "budget")
public class Budget {

    @Id
    @GeneratedValue
    private Long budgetId;

    private BigDecimal budgetAmount;

    @Enumerated(EnumType.STRING)
    private Category budgetCategory;

    private BigDecimal budgetSpent;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
