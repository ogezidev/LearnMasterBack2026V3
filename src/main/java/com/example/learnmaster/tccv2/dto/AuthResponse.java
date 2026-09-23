package com.example.learnmaster.tccv2.dto;

// Resposta de login/cadastro/refresh: o refresh token vai so no cookie httpOnly
public record AuthResponse(String accessToken, long expiraEmSegundos, UsuarioResponse usuario) {
}
