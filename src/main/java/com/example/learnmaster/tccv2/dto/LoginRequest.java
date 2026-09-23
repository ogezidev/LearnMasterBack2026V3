package com.example.learnmaster.tccv2.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank(message = "Informe seu e-mail.") String email,
        @NotBlank(message = "Informe sua senha.") String senha,
        Boolean lembrar) {
}
