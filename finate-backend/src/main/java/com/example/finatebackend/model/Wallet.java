package com.example.finatebackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "wallet")
public class Wallet {

    @Id
    @GeneratedValue
    private Long walletId;

    private BigDecimal currentBalance;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WalletTransaction> walletTransactionList = new ArrayList<>();

    @OneToOne(mappedBy = "userWallet")
    private User walletUser;
}
