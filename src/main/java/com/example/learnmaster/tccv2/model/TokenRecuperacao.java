package com.example.learnmaster.tccv2.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

// Codigo de 6 digitos para redefinir a senha: so o HMAC fica no banco; vale 30 min, uma vez e ate 5 erros
@Entity
@Table(name = "TokenRecuperacao")
public class TokenRecuperacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "usuario_id")
    private Integer usuarioId;

    @Column(name = "token_hash")
    private String tokenHash;

    @Column(name = "expira_em")
    private LocalDateTime expiraEm;

    @Column(name = "usado_em")
    private LocalDateTime usadoEm;

    // Codigos errados digitados para este pedido
    @Column(name = "tentativas")
    private int tentativas;

    // Usado para limitar pedidos de recuperacao por e-mail
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    public TokenRecuperacao() {
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

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public void setExpiraEm(LocalDateTime expiraEm) {
        this.expiraEm = expiraEm;
    }

    public LocalDateTime getUsadoEm() {
        return usadoEm;
    }

    public void setUsadoEm(LocalDateTime usadoEm) {
        this.usadoEm = usadoEm;
    }

    public int getTentativas() {
        return tentativas;
    }

    public void setTentativas(int tentativas) {
        this.tentativas = tentativas;
    }

    @PrePersist
    void aoCriar() {
        if (criadoEm == null) criadoEm = LocalDateTime.now(ZoneOffset.UTC);
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
