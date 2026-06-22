package com.example.finatebackend.dto.authdto;

import com.example.finatebackend.enums.UserRole;
import jakarta.validation.constraints.NotBlank;


public record RegisterResponseDto(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank UserRole userRole,
        @NotBlank String username
) {
}
