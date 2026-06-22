package com.example.finatebackend.controller;

import com.example.finatebackend.dto.walletdto.PaymentRequestDto;
import com.example.finatebackend.dto.walletdto.PaymentResponseDto;
import com.example.finatebackend.dto.walletdto.PaymentVerifyRequestDto;
import com.example.finatebackend.dto.walletdto.WalletSummaryResponseDto;
import com.example.finatebackend.service.WalletService;
import com.razorpay.RazorpayException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<PaymentResponseDto> createOrder(@RequestBody PaymentRequestDto paymentRequestDto) throws RazorpayException {
        return ResponseEntity.ok(walletService.createOrder(paymentRequestDto));
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<String> verifyPayment(@RequestBody PaymentVerifyRequestDto paymentVerifyRequestDto) throws RazorpayException {
        return ResponseEntity.ok(walletService.verifyPayment(paymentVerifyRequestDto));
    }

    @GetMapping("/get-wallet-details")
    public ResponseEntity<WalletSummaryResponseDto> getWalletDetails() {
        return ResponseEntity.ok(walletService.getWalletDetails());
    }
}
