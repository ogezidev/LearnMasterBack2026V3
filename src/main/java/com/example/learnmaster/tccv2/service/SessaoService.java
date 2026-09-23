package com.example.learnmaster.tccv2.service;

import com.example.learnmaster.tccv2.dto.AuthResponse;
import com.example.learnmaster.tccv2.dto.UsuarioResponse;
import com.example.learnmaster.tccv2.exception.ApiException;
import com.example.learnmaster.tccv2.model.RefreshToken;
import com.example.learnmaster.tccv2.model.Usuario;
import com.example.learnmaster.tccv2.repository.RefreshTokenRepository;
import com.example.learnmaster.tccv2.security.Tokens;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/*
 * Sessao em duas partes:
 * - token de acesso (JWT, 15 min): o frontend guarda so em memoria e envia no header Authorization;
 * - refresh token (aleatorio): vai em cookie httpOnly e so o hash fica no banco. Cada uso gera um novo
 *   (o anterior e revogado). Com "Continuar logado" o cookie dura 30 dias; sem, e cookie de sessao,
 *   apagado quando o navegador fecha.
 */
@Service
public class SessaoService {

    public static final String COOKIE = "lm_refresh";
    private static final Duration DURACAO_ACESSO = Duration.ofMinutes(15);
    private static final Duration DURACAO_PERSISTENTE = Duration.ofDays(30);
    private static final Duration DURACAO_SESSAO = Duration.ofHours(12);

    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final boolean cookieSecure;
    private final String cookieSameSite;

    public SessaoService(JwtEncoder jwtEncoder,
                         RefreshTokenRepository refreshTokenRepository,
                         @Value("${app.cookie.secure}") boolean cookieSecure,
                         @Value("${app.cookie.same-site}") String cookieSameSite) {
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
    }

    public static LocalDateTime agoraUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    // Cria uma sessao nova e monta a resposta com o cookie
    @Transactional
    public ResponseEntity<AuthResponse> iniciar(Usuario usuario, boolean persistente, HttpStatus status) {
        String refresh = criarRefresh(usuario.getId(), persistente);
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie(refresh, persistente).toString())
                .body(resposta(usuario));
    }

    // Valida o refresh token recebido, revoga-o e devolve o id do usuario e um token novo
    @Transactional
    public Renovacao renovar(String refreshRecebido) {
        RefreshToken atual = refreshRecebido == null ? null
                : refreshTokenRepository.findByTokenHash(Tokens.hash(refreshRecebido)).orElse(null);
        LocalDateTime agora = agoraUtc();
        if (atual == null || atual.getRevogadoEm() != null || atual.getExpiraEm().isBefore(agora)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Sessão expirada. Entre novamente.");
        }
        atual.setRevogadoEm(agora);
        String novo = criarRefresh(atual.getUsuarioId(), atual.isPersistente());
        return new Renovacao(atual.getUsuarioId(), novo, atual.isPersistente());
    }

    public record Renovacao(Integer usuarioId, String refresh, boolean persistente) {
    }

    // Diz se a sessao atual e "continuar logado" (para manter o mesmo tipo ao recriar)
    public boolean persistente(String refreshRecebido) {
        if (refreshRecebido == null) return false;
        return refreshTokenRepository.findByTokenHash(Tokens.hash(refreshRecebido))
                .map(RefreshToken::isPersistente)
                .orElse(false);
    }

    @Transactional
    public void revogar(String refreshRecebido) {
        if (refreshRecebido == null) return;
        refreshTokenRepository.findByTokenHash(Tokens.hash(refreshRecebido)).ifPresent(r -> {
            if (r.getRevogadoEm() == null) r.setRevogadoEm(agoraUtc());
        });
    }

    // Derruba todas as sessoes do usuario (troca ou redefinicao de senha)
    @Transactional
    public void revogarTodas(Integer usuarioId) {
        refreshTokenRepository.revogarTodos(usuarioId, agoraUtc());
    }

    public AuthResponse resposta(Usuario usuario) {
        return new AuthResponse(gerarAcesso(usuario), DURACAO_ACESSO.toSeconds(), UsuarioResponse.of(usuario));
    }

    public ResponseCookie cookie(String refresh, boolean persistente) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(COOKIE, refresh)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/auth");
        if (persistente) builder.maxAge(DURACAO_PERSISTENTE);
        return builder.build();
    }

    public ResponseCookie cookieVazio() {
        return ResponseCookie.from(COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/auth")
                .maxAge(0)
                .build();
    }

    private String gerarAcesso(Usuario usuario) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("learnmaster")
                .issuedAt(agora)
                .expiresAt(agora.plus(DURACAO_ACESSO))
                .subject(String.valueOf(usuario.getId()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private String criarRefresh(Integer usuarioId, boolean persistente) {
        String valor = Tokens.gerar();
        RefreshToken token = new RefreshToken();
        token.setUsuarioId(usuarioId);
        token.setTokenHash(Tokens.hash(valor));
        token.setPersistente(persistente);
        token.setExpiraEm(agoraUtc().plus(persistente ? DURACAO_PERSISTENTE : DURACAO_SESSAO));
        refreshTokenRepository.save(token);
        return valor;
    }
}
