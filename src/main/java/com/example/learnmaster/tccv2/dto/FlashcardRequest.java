package com.example.learnmaster.tccv2.dto;

import jakarta.validation.constraints.*;

public record FlashcardRequest(
        @NotBlank(message = "Preencha a frente do card.")
        @Size(max = 200, message = "A frente pode ter no máximo 200 caracteres.")
        String frente,

        @NotBlank(message = "Preencha o verso do card.")
        @Size(max = 200, message = "O verso pode ter no máximo 200 caracteres.")
        String verso,

        @NotNull(message = "Escolha o deck.")
        Integer deckId) {
}
