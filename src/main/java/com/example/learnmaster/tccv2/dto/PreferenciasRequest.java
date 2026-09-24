package com.example.learnmaster.tccv2.dto;

// Campos nulos ficam como estao (da para mudar so uma preferencia)
public record PreferenciasRequest(Boolean modoNoturno, Boolean fonteDislexia, Boolean tutorialConcluido) {
}
