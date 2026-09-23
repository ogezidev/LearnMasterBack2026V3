package com.example.learnmaster.tccv2.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Flashcard")
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String frente;
    private String verso;

    @Column(name = "deck_id")
    private Integer deckId;


    // Preenchido pelo banco (DEFAULT SYSUTCDATETIME()), em UTC
    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    public Flashcard() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getFrente() {
        return frente;
    }

    public void setFrente(String frente) {
        this.frente = frente;
    }

    public String getVerso() {
        return verso;
    }

    public void setVerso(String verso) {
        this.verso = verso;
    }

    public Integer getDeckId() {
        return deckId;
    }

    public void setDeckId(Integer deckId) {
        this.deckId = deckId;
    }


    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}