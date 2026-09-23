package com.example.learnmaster.tccv2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Deck")
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;

    @Column(name = "main_deck_id")
    private Integer mainDeckId;

    @Column(name = "usuario_id")
    private Integer usuarioId;

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

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }
}