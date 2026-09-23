package com.example.learnmaster.tccv2.dto;

import jakarta.validation.constraints.*;

public record RecuperarSenhaRequest(
        @NotBlank(message = "Informe seu e-mail.")
        @Email(message = "Informe um e-mail válido.")
        String email) {
}
