package com.example.finatebackend.dao;

import com.example.finatebackend.model.Wallet;
import com.example.finatebackend.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionsRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findAllByWallet(Wallet wallet);
}