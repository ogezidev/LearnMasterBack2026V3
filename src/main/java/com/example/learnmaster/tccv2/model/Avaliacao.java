package com.example.learnmaster.tccv2.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

// Historico completo: a avaliacao atual de um card e a mais recente (maior avaliado_em)
@Entity
@Table(name = "Avaliacao")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "usuario_id")
    private Integer usuarioId;

    @Column(name = "flashcard_id")
    private Integer flashcardId;

    // 'dificil', 'bom' ou 'facil' (CHECK no banco)
    private String nivel;

    @Column(name = "avaliado_em", updatable = false)
    private LocalDateTime avaliadoEm;

    public Avaliacao() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Integer getFlashcardId() {
        return flashcardId;
    }

    public void setFlashcardId(Integer flashcardId) {
        this.flashcardId = flashcardId;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    @PrePersist
    void aoCriar() {
        if (avaliadoEm == null) avaliadoEm = LocalDateTime.now(ZoneOffset.UTC);
    }

    public LocalDateTime getAvaliadoEm() {
        return avaliadoEm;
    }
}
