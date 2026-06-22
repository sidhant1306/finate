package com.example.finatebackend.controller;

import com.example.finatebackend.dto.walletdto.UpiPaymentRequestDto;
import com.example.finatebackend.dto.walletdto.UpiPaymentResponseDto;
import com.example.finatebackend.service.UpiPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/upi")
public class UpiController {

    private final UpiPaymentService upiPaymentService;

    public UpiController(UpiPaymentService upiPaymentService) {
        this.upiPaymentService = upiPaymentService;
    }

    @PostMapping("/send-money")
    public ResponseEntity<UpiPaymentResponseDto> sendMoney(@RequestBody UpiPaymentRequestDto requestDto) {
        return ResponseEntity.ok(upiPaymentService.sendMoney(requestDto));
    }
}
