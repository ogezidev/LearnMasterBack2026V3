package com.example.learnmaster.tccv2.dto;

import jakarta.validation.constraints.*;

public record AlterarSenhaRequest(
        @NotBlank(message = "Informe sua senha atual.") String senhaAtual,
        @NotNull(message = "Informe a nova senha.") String novaSenha) {
}
