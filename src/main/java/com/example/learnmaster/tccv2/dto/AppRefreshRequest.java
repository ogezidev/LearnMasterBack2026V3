package com.example.learnmaster.tccv2.dto;

import jakarta.validation.constraints.NotBlank;

public record AppRefreshRequest(@NotBlank(message = "Sessão expirada. Entre novamente.") String refreshToken) {
}
