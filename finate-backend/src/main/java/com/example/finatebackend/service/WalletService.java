package com.example.finatebackend.service;

import com.example.finatebackend.dao.UserRepository;
import com.example.finatebackend.dao.WalletRepository;
import com.example.finatebackend.dao.WalletTransactionsRepository;
import com.example.finatebackend.dto.walletdto.*;
import com.example.finatebackend.enums.PaymentType;
import com.example.finatebackend.enums.TransactionStatus;
import com.example.finatebackend.enums.TransactionType;
import com.example.finatebackend.enums.UserRole;
import com.example.finatebackend.exceptions.NotFoundException;
import com.example.finatebackend.exceptions.PaymentVerificationException;
import com.example.finatebackend.model.User;
import com.example.finatebackend.model.Wallet;
import com.example.finatebackend.model.WalletTransaction;
import com.example.finatebackend.security.SecurityUtils;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Service
public class WalletService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionsRepository walletTransactionsRepository;
    private final EmailService emailService;
    @Value("${razorpay.key.id}")
    private String razorpayKey;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    public static final BigDecimal PREMIUM_AMOUNT = new BigDecimal("99");

    public WalletService(UserRepository userRepository, WalletRepository walletRepository, WalletTransactionsRepository walletTransactionsRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionsRepository = walletTransactionsRepository;
        this.emailService = emailService;
    }


    public PaymentResponseDto createOrder(PaymentRequestDto paymentRequestDto) throws RazorpayException {
        RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);

        BigDecimal amount = paymentRequestDto.paymentType().equals(PaymentType.PREMIUM)
                ? PREMIUM_AMOUNT : paymentRequestDto.paymentAmount();

        int amountInPaise = amount.multiply(new BigDecimal("100")).intValue();

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount",amountInPaise);
        orderRequest.put("receipt", "finate_" + System.currentTimeMillis());
        orderRequest.put("currency", "INR");

        Order order = client.orders.create(orderRequest);

        return new PaymentResponseDto(
            order.get("id").toString(),
                amount,
                "INR",
                razorpayKey
        );
    }

    @Transactional
    public String verifyPayment(PaymentVerifyRequestDto paymentVerifyRequestDto) throws RazorpayException {
        String generatedSignature = Utils.getHash(
                paymentVerifyRequestDto.orderId() + "|" + paymentVerifyRequestDto.paymentId(),
                razorpaySecret
        );

        if(!(generatedSignature.equals(paymentVerifyRequestDto.signature()))) {
            throw new PaymentVerificationException("Payment verification failed");
        }

        User user = SecurityUtils.getCurrentUser();

        if(paymentVerifyRequestDto.paymentType().equals(PaymentType.PREMIUM)) {
           upgradeUser(user);
        }
        else {
            handleWalletDeposit(user, paymentVerifyRequestDto.amount());
        }

        return "Payment successful";
    }

    private void upgradeUser(User user) {
        user.setUserRole(UserRole.PREMIUM);
        userRepository.save(user);
        Wallet wallet = walletRepository.findByWalletUser(user).orElseThrow(
                () -> new NotFoundException("Wallet not found for user")
        );
        WalletTransaction walletTransaction =  WalletTransaction
                .builder()
                .paymentType(PaymentType.PREMIUM)
                .transactionType(TransactionType.DEBIT)
                .transactionStatus(TransactionStatus.SUCCESS)
                .amount(PREMIUM_AMOUNT)
                .transactionDate(LocalDate.now())
                .wallet(wallet)
                .build();

        walletTransactionsRepository.save(walletTransaction);
        emailService.sendPremiumActivationEmail(user.getUserEmail(), user.getFirstName());
    }

    public WalletSummaryResponseDto getWalletDetails() {
        User user = SecurityUtils.getCurrentUser();
        Wallet wallet = walletRepository.findByWalletUser(user).orElseThrow(
                () -> new NotFoundException("Wallet not found :)")
        );

        List<WalletTransaction> walletTransactionList = walletTransactionsRepository.findAllByWallet(wallet);

        List<WalletTransactionResponseDto> walletTransactionResponseDtoList = walletTransactionList.stream()
                .map(transaction -> new WalletTransactionResponseDto(
                        transaction.getPaymentType(),
                        transaction.getTransactionType(),
                        transaction.getTransactionStatus(),
                        transaction.getAmount(),
                        transaction.getRecipientId(),
                        transaction.getTransactionDate()
                )).toList();

        return new WalletSummaryResponseDto(
                walletTransactionResponseDtoList,
                wallet.getCurrentBalance()
        );
    }

    private void handleWalletDeposit(User user, BigDecimal amount) {
        Wallet wallet = walletRepository.findByWalletUser(user).orElseThrow(
                () -> new NotFoundException("Wallet for user with user id : " +user.getUserId() + " not found")
        );

        wallet.setCurrentBalance(wallet.getCurrentBalance().add(amount));
        walletRepository.save(wallet);

        WalletTransaction walletTransaction = WalletTransaction.builder()
                .paymentType(PaymentType.WALLET)
                .transactionType(TransactionType.DEPOSIT)
                .wallet(wallet)
                .amount(amount)
                .transactionDate(LocalDate.now())
                .recipientId(user.getUserId())
                .transactionStatus(TransactionStatus.SUCCESS)
                .build();

        walletTransactionsRepository.save(walletTransaction);
        emailService.sendWalletDepositEmail(user.getUserEmail(), user.getFirstName(), amount, wallet.getCurrentBalance());
    }
}
