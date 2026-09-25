package com.example.learnmaster.tccv2.controller;

import com.example.learnmaster.tccv2.dto.AppAuthResponse;
import com.example.learnmaster.tccv2.dto.AppRefreshRequest;
import com.example.learnmaster.tccv2.dto.LoginRequest;
import com.example.learnmaster.tccv2.model.Usuario;
import com.example.learnmaster.tccv2.service.AuthService;
import com.example.learnmaster.tccv2.service.ContaService;
import com.example.learnmaster.tccv2.service.SessaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/*
 * Sessao do app mobile. Mesmas regras do login da web (BCrypt, limite de tentativas, refresh
 * com rotacao e hash no banco), mas o refresh token vai no corpo em vez do cookie: no celular
 * ele fica no armazenamento seguro do sistema (Keychain / Keystore).
 * Nao ha cadastro aqui: contas sao criadas so pela web.
 */
@RestController
@RequestMapping("/auth/app")
public class AppAuthController {

    private final AuthService authService;
    private final SessaoService sessaoService;
    private final ContaService contaService;

    public AppAuthController(AuthService authService, SessaoService sessaoService, ContaService contaService) {
        this.authService = authService;
        this.sessaoService = sessaoService;
        this.contaService = contaService;
    }

    // No app a sessao sempre dura 30 dias (continuar logado)
    @PostMapping("/login")
    public AppAuthResponse login(@Valid @RequestBody LoginRequest req) {
        Usuario usuario = authService.autenticar(req.email(), req.senha());
        return sessaoService.iniciarApp(usuario);
    }

    @PostMapping("/refresh")
    public AppAuthResponse refresh(@Valid @RequestBody AppRefreshRequest req) {
        SessaoService.Renovacao renovacao = sessaoService.renovar(req.refreshToken());
        Usuario usuario = contaService.buscar(renovacao.usuarioId());
        return sessaoService.respostaApp(usuario, renovacao.refresh());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody(required = false) AppRefreshRequest req) {
        if (req != null && req.refreshToken() != null && !req.refreshToken().isBlank()) {
            sessaoService.revogar(req.refreshToken());
        }
    }
}
