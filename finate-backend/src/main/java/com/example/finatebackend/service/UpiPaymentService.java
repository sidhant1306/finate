package com.example.finatebackend.service;

import com.example.finatebackend.dao.UserRepository;
import com.example.finatebackend.dao.WalletRepository;
import com.example.finatebackend.dao.WalletTransactionsRepository;
import com.example.finatebackend.dto.walletdto.UpiPaymentRequestDto;
import com.example.finatebackend.dto.walletdto.UpiPaymentResponseDto;
import com.example.finatebackend.enums.PaymentType;
import com.example.finatebackend.enums.TransactionStatus;
import com.example.finatebackend.enums.TransactionType;
import com.example.finatebackend.exceptions.InsufficientFundsException;
import com.example.finatebackend.exceptions.NotFoundException;
import com.example.finatebackend.exceptions.UserNotFoundException;
import com.example.finatebackend.model.User;
import com.example.finatebackend.model.Wallet;
import com.example.finatebackend.model.WalletTransaction;
import com.example.finatebackend.security.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UpiPaymentService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionsRepository walletTransactionsRepository;


    public UpiPaymentService(UserRepository userRepository, WalletRepository walletRepository, WalletTransactionsRepository walletTransactionsRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionsRepository = walletTransactionsRepository;
    }

    @Transactional
    public UpiPaymentResponseDto sendMoney(UpiPaymentRequestDto requestDto) {
        User sender = SecurityUtils.getCurrentUser();
        Wallet senderWallet = walletRepository.findByWalletUser(sender).orElseThrow(
                () -> new NotFoundException("wallet with user id : " + sender.getUserId() + " not found")
        );
        User receiver = userRepository.findByUserId(requestDto.receiverUserid()).orElseThrow(
                () -> new UserNotFoundException("user with id : " + requestDto.receiverUserid() + " not found")
        );

        if(sender.getUserId().equals(receiver.getUserId())) {
            throw new IllegalArgumentException("sender and receiver users are the same");
        }
        Wallet receiverWallet = walletRepository.findByWalletUser(receiver).orElseThrow(
                () -> new NotFoundException("wallet with user id : " + receiver.getUserId() + " not found")
        );

        if(senderWallet.getCurrentBalance().compareTo(requestDto.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        WalletTransaction transaction = WalletTransaction.builder()
                .paymentType(PaymentType.WALLET)
                .transactionType(TransactionType.UPI)
                .transactionStatus(TransactionStatus.SUCCESS)
                .amount(requestDto.amount())
                .recipientId(requestDto.receiverUserid())
                .transactionDate(LocalDate.now())
                .wallet(senderWallet)
                .build();

        walletTransactionsRepository.save(transaction);

        // creating receiver's transaction :
        WalletTransaction receiverTransaction = WalletTransaction.builder()
                .paymentType(PaymentType.WALLET)
                .transactionType(TransactionType.CREDIT)
                .transactionStatus(TransactionStatus.SUCCESS)
                .amount(requestDto.amount())
                .recipientId(sender.getUserId())    // sender id
                .transactionDate(LocalDate.now())
                .wallet(receiverWallet)
                .build();
        senderWallet.setCurrentBalance(senderWallet.getCurrentBalance().subtract(requestDto.amount()));
        walletRepository.save(senderWallet);
        receiverWallet.setCurrentBalance(receiverWallet.getCurrentBalance().add(requestDto.amount()));
        walletRepository.save(receiverWallet);
        walletTransactionsRepository.save(receiverTransaction);

        return new UpiPaymentResponseDto(
                System.currentTimeMillis(),
                requestDto.receiverUserid(),
                sender.getUserId(),
                requestDto.amount(),
                receiver.getFirstName()
        );
    }
}
