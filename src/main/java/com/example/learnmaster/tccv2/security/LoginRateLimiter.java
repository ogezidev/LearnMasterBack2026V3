package com.example.learnmaster.tccv2.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Bloqueia o e-mail por 15 min depois de 5 senhas erradas. Fica em memoria: zera ao reiniciar o backend.
@Component
public class LoginRateLimiter {

    private static final int MAX_FALHAS = 5;
    private static final Duration JANELA = Duration.ofMinutes(15);

    private record Tentativas(int falhas, Instant inicio) {
    }

    private final Map<String, Tentativas> porEmail = new ConcurrentHashMap<>();

    public boolean bloqueado(String email) {
        Tentativas t = porEmail.get(email);
        if (t == null) return false;
        if (t.inicio().plus(JANELA).isBefore(Instant.now())) {
            porEmail.remove(email);
            return false;
        }
        return t.falhas() >= MAX_FALHAS;
    }

    public void registrarFalha(String email) {
        porEmail.merge(email, new Tentativas(1, Instant.now()), (atual, nova) ->
                atual.inicio().plus(JANELA).isBefore(Instant.now())
                        ? nova
                        : new Tentativas(atual.falhas() + 1, atual.inicio()));
    }

    public void limpar(String email) {
        porEmail.remove(email);
    }
}
