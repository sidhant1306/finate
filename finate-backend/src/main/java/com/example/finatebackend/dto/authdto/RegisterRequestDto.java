package com.example.finatebackend.dto.authdto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;



public record RegisterRequestDto(
        @NotBlank(message = "First name is mandatory")
        String firstName,
        String lastName,
        @NotBlank(message = "A unique username is required")
        String username,
        @Email(message = "Please enter a valid email address")
        String userEmail,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String userPassword
) {

}
