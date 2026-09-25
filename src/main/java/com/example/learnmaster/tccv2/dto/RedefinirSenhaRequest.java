package com.example.learnmaster.tccv2.dto;

import jakarta.validation.constraints.*;

public record RedefinirSenhaRequest(
        @NotBlank(message = "Informe o e-mail.") String email,
        @NotBlank(message = "Informe o código de 6 dígitos.")
        @Pattern(regexp = "\\d{6}", message = "O código tem 6 dígitos.") String codigo,
        @NotNull(message = "Informe a nova senha.") String novaSenha) {
}
