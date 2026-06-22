package com.example.finatebackend.dto.authdto;

import com.example.finatebackend.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;


public record LoginResponseDto(
        @NotBlank String token,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String userEmail,
        @NotBlank UserRole userRole,
        @NotBlank String username
) {
}
