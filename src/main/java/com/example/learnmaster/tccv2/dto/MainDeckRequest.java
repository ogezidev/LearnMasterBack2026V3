package com.example.learnmaster.tccv2.dto;

import jakarta.validation.constraints.*;

public record MainDeckRequest(
        @NotBlank(message = "Informe o nome do LearnDeck.")
        @Size(max = 50, message = "O nome do LearnDeck pode ter no máximo 50 caracteres.")
        String nome) {
}
