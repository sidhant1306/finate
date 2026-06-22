package com.example.finatebackend.dto.authdto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank(message = "User email is required")
        String userEmail,

        @NotBlank(message = "Password is mandatory")
        String userPassword
) {
}
