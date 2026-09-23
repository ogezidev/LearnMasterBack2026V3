package com.example.learnmaster.tccv2.controller;

import com.example.learnmaster.tccv2.dto.*;
import com.example.learnmaster.tccv2.exception.ApiException;
import com.example.learnmaster.tccv2.model.Usuario;
import com.example.learnmaster.tccv2.service.AuthService;
import com.example.learnmaster.tccv2.service.ContaService;
import com.example.learnmaster.tccv2.service.SessaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final SessaoService sessaoService;
    private final ContaService contaService;

    public AuthController(AuthService authService, SessaoService sessaoService, ContaService contaService) {
        this.authService = authService;
        this.sessaoService = sessaoService;
        this.contaService = contaService;
    }

    // Cria a conta e ja entra (sessao que termina ao fechar o navegador)
    @PostMapping("/cadastro")
    public ResponseEntity<AuthResponse> cadastro(@Valid @RequestBody CadastroRequest req) {
        Usuario usuario = authService.cadastrar(req);
        return sessaoService.iniciar(usuario, false, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        Usuario usuario = authService.autenticar(req.email(), req.senha());
        return sessaoService.iniciar(usuario, Boolean.TRUE.equals(req.lembrar()), HttpStatus.OK);
    }

    // Chamado ao abrir o app e quando o token de acesso expira
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = SessaoService.COOKIE, required = false) String refresh) {
        if (refresh == null || refresh.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Sessão expirada. Entre novamente.");
        }
        SessaoService.Renovacao renovacao = sessaoService.renovar(refresh);
        Usuario usuario = contaService.buscar(renovacao.usuarioId());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessaoService.cookie(renovacao.refresh(), renovacao.persistente()).toString())
                .body(sessaoService.resposta(usuario));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = SessaoService.COOKIE, required = false) String refresh) {
        if (refresh != null && !refresh.isBlank()) sessaoService.revogar(refresh);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, sessaoService.cookieVazio().toString())
                .build();
    }

    // Mesma resposta exista ou nao o e-mail
    @PostMapping("/recuperar")
    public Map<String, String> recuperar(@Valid @RequestBody RecuperarSenhaRequest req) {
        authService.solicitarRecuperacao(req.email());
        return Map.of("mensagem", "Se o e-mail estiver cadastrado, você receberá um link para redefinir a senha.");
    }

    @PostMapping("/redefinir")
    public Map<String, String> redefinir(@Valid @RequestBody RedefinirSenhaRequest req) {
        authService.redefinirSenha(req.token(), req.novaSenha());
        return Map.of("mensagem", "Senha redefinida com sucesso. Entre com a nova senha.");
    }
}
