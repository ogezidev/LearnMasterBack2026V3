package com.example.learnmaster.tccv2.security;

import com.example.learnmaster.tccv2.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;

// Mesmas regras mostradas na tela de cadastro
public final class SenhaPolicy {

    // Limite tecnico do BCrypt: so os primeiros 72 bytes contam
    private static final int MAX_BYTES = 72;

    private SenhaPolicy() {
    }

    public static void validar(String senha) {
        if (senha == null || senha.length() < 8
                || !senha.matches(".*[A-Z].*")
                || !senha.matches(".*[a-z].*")
                || !senha.matches(".*[0-9].*")
                || !senha.matches(".*[^A-Za-z0-9].*")) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "A senha precisa ter no mínimo 8 caracteres, com letra maiúscula, letra minúscula, número e caractere especial.");
        }
        if (senha.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A senha é longa demais.");
        }
    }
}
