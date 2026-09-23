package com.example.learnmaster.tccv2.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Deck")
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;

    @Column(name = "main_deck_id")
    private Integer mainDeckId;


    // Preenchido pelo banco (DEFAULT SYSUTCDATETIME()), em UTC
    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    public Deck() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getMainDeckId() {
        return mainDeckId;
    }

    public void setMainDeckId(Integer mainDeckId) {
        this.mainDeckId = mainDeckId;
    }


    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}