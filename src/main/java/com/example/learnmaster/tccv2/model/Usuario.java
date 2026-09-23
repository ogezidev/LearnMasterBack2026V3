package com.example.learnmaster.tccv2.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String email;
    private String nome;
    private String senha;

    @Column(name = "modo_noturno")
    private boolean modoNoturno;

    @Column(name = "fonte_dislexia")
    private boolean fonteDislexia;

    @Column(name = "tutorial_concluido")
    private boolean tutorialConcluido;

    @Column(name = "ultimo_deck_id")
    private Integer ultimoDeckId;

    // Preenchido pelo banco (DEFAULT SYSUTCDATETIME()), em UTC
    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    public Usuario() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean isModoNoturno() {
        return modoNoturno;
    }

    public void setModoNoturno(boolean modoNoturno) {
        this.modoNoturno = modoNoturno;
    }

    public boolean isFonteDislexia() {
        return fonteDislexia;
    }

    public void setFonteDislexia(boolean fonteDislexia) {
        this.fonteDislexia = fonteDislexia;
    }

    public boolean isTutorialConcluido() {
        return tutorialConcluido;
    }

    public void setTutorialConcluido(boolean tutorialConcluido) {
        this.tutorialConcluido = tutorialConcluido;
    }

    public Integer getUltimoDeckId() {
        return ultimoDeckId;
    }

    public void setUltimoDeckId(Integer ultimoDeckId) {
        this.ultimoDeckId = ultimoDeckId;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}