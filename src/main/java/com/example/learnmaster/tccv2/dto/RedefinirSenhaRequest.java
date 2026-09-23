package com.example.learnmaster.tccv2.dto;

import jakarta.validation.constraints.*;

public record RedefinirSenhaRequest(
        @NotBlank(message = "Link inválido ou expirado. Peça um novo.") String token,
        @NotNull(message = "Informe a nova senha.") String novaSenha) {
}
