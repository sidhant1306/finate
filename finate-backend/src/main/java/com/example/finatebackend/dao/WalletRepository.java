package com.example.finatebackend.dao;

import com.example.finatebackend.model.User;
import com.example.finatebackend.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByWalletUser(User user);
}