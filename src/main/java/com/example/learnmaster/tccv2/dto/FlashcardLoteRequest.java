package com.example.learnmaster.tccv2.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

// Criacao de ate 5 cards de uma vez no mesmo deck (tela Criar)
public record FlashcardLoteRequest(
        @NotNull(message = "Escolha o deck.")
        Integer deckId,

        @NotNull(message = "Adicione pelo menos um card.")
        @Size(min = 1, max = 5, message = "Crie de 1 a 5 cards por vez.")
        List<@Valid Conteudo> cards) {

    public record Conteudo(
            @NotBlank(message = "Preencha a frente de todos os cards.")
            @Size(max = 200, message = "A frente pode ter no máximo 200 caracteres.")
            String frente,

            @NotBlank(message = "Preencha o verso de todos os cards.")
            @Size(max = 200, message = "O verso pode ter no máximo 200 caracteres.")
            String verso) {
    }
}
