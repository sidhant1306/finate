package com.example.finatebackend.dto.authdto;

public record VerifyOtpRequestDto(
        Long otp,
        String userEmail
) {
}
