package com.example.learnmaster.tccv2.dto;

// Resposta de login/refresh do app mobile: o refresh token vai no corpo (o app guarda no cofre do aparelho)
public record AppAuthResponse(String accessToken, long expiraEmSegundos, String refreshToken, UsuarioResponse usuario) {
}
