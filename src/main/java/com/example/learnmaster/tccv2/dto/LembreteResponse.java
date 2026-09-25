package com.example.learnmaster.tccv2.dto;

import com.example.learnmaster.tccv2.model.Lembrete;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public record LembreteResponse(
        Integer id,
        String titulo,
        Integer deckId,
        Lembrete.Frequencia frequencia,
        LocalDate data,
        @JsonFormat(pattern = "HH:mm") LocalTime horario,
        List<Integer> diasSemana,
        boolean ativo) {

    public static LembreteResponse of(Lembrete l) {
        List<Integer> dias = new ArrayList<>();
        for (int dia = 1; dia <= 7; dia++) {
            if ((l.getDiasSemana() & (1 << (dia - 1))) != 0) dias.add(dia);
        }
        return new LembreteResponse(l.getId(), l.getTitulo(), l.getDeckId(), l.getFrequencia(),
                l.getData(), l.getHorario(), dias, l.isAtivo());
    }
}
