package com.example.learnmaster.tccv2.dto;

import com.example.learnmaster.tccv2.model.Lembrete;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// diasSemana: 1 = segunda ... 7 = domingo (so para SEMANAL). data: so para UMA_VEZ
public record LembreteRequest(
        @NotBlank(message = "Informe o título do lembrete.")
        @Size(max = 60, message = "O título pode ter no máximo 60 caracteres.")
        String titulo,

        Integer deckId,

        @NotNull(message = "Escolha a frequência.")
        Lembrete.Frequencia frequencia,

        LocalDate data,

        @NotNull(message = "Informe o horário.")
        LocalTime horario,

        List<@NotNull @Min(value = 1, message = "Dia da semana inválido.") @Max(value = 7, message = "Dia da semana inválido.") Integer> diasSemana,

        Boolean ativo) {
}
