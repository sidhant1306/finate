package com.example.finatebackend.controller;

import com.example.finatebackend.dto.authdto.*;
import com.example.finatebackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody RegisterRequestDto registerRequestDto) {
        return ResponseEntity.ok(authService.register(registerRequestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Boolean> verifyOtp(@RequestBody VerifyOtpRequestDto verifyOtpRequestDto) {
        return ResponseEntity.ok(authService.verifyOtp(verifyOtpRequestDto));
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponseDto> fetchMe() {
        return ResponseEntity.ok(authService.fetchMe());
    }
}
