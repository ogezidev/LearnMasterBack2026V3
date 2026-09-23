package com.example.learnmaster.tccv2.dto;

import jakarta.validation.constraints.*;

public record DeckRequest(
        @NotBlank(message = "Informe o nome do deck.")
        @Size(max = 50, message = "O nome do deck pode ter no máximo 50 caracteres.")
        String nome,

        @NotNull(message = "Escolha o LearnDeck.")
        Integer mainDeckId) {
}
