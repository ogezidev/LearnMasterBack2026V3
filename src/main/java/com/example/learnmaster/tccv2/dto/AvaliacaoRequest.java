package com.example.learnmaster.tccv2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AvaliacaoRequest(
        @NotNull(message = "Informe o card.")
        Integer flashcardId,

        @NotBlank(message = "Escolha Difícil, Bom ou Fácil.")
        @Pattern(regexp = "dificil|bom|facil", message = "Escolha Difícil, Bom ou Fácil.")
        String nivel) {
}
