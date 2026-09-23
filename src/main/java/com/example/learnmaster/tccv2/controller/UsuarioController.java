package com.example.learnmaster.tccv2.controller;

import com.example.learnmaster.tccv2.dto.*;
import com.example.learnmaster.tccv2.model.Usuario;
import com.example.learnmaster.tccv2.security.UsuarioLogado;
import com.example.learnmaster.tccv2.service.ContaService;
import com.example.learnmaster.tccv2.service.SessaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

// So o proprio usuario logado: nao existe mais rota para listar, alterar ou apagar outros usuarios
@RestController
@RequestMapping("/usuarios/me")
public class UsuarioController {

    private final ContaService contaService;
    private final SessaoService sessaoService;

    public UsuarioController(ContaService contaService, SessaoService sessaoService) {
        this.contaService = contaService;
        this.sessaoService = sessaoService;
    }

    @GetMapping
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
        return UsuarioResponse.of(contaService.buscar(UsuarioLogado.id(jwt)));
    }

    @PatchMapping
    public UsuarioResponse atualizarNome(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AtualizarNomeRequest req) {
        return UsuarioResponse.of(contaService.atualizarNome(UsuarioLogado.id(jwt), req.nome()));
    }

    @PutMapping("/email")
    public UsuarioResponse alterarEmail(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AlterarEmailRequest req) {
        return UsuarioResponse.of(contaService.alterarEmail(UsuarioLogado.id(jwt), req.email(), req.senhaAtual()));
    }

    // Derruba todas as sessoes (outros dispositivos saem) e abre uma nova neste navegador
    @PutMapping("/senha")
    public ResponseEntity<AuthResponse> alterarSenha(@AuthenticationPrincipal Jwt jwt,
                                                     @CookieValue(name = SessaoService.COOKIE, required = false) String refresh,
                                                     @Valid @RequestBody AlterarSenhaRequest req) {
        boolean persistente = sessaoService.persistente(refresh);
        Usuario usuario = contaService.alterarSenha(UsuarioLogado.id(jwt), req.senhaAtual(), req.novaSenha());
        return sessaoService.iniciar(usuario, persistente, HttpStatus.OK);
    }
}
