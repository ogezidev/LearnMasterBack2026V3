package com.example.learnmaster.tccv2.dto;

import jakarta.validation.constraints.*;

public record AtualizarNomeRequest(
        @NotBlank(message = "Informe seu nome.")
        @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres.")
        String nome) {
}
