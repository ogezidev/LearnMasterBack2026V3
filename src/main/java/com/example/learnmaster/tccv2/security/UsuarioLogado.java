package com.example.learnmaster.tccv2.security;

import org.springframework.security.oauth2.jwt.Jwt;

// O "subject" do token de acesso e o id do usuario
public final class UsuarioLogado {

    private UsuarioLogado() {
    }

    public static Integer id(Jwt jwt) {
        return Integer.valueOf(jwt.getSubject());
    }
}
