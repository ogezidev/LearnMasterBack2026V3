package com.example.learnmaster.tccv2.service;

import com.example.learnmaster.tccv2.dto.CadastroRequest;
import com.example.learnmaster.tccv2.exception.ApiException;
import com.example.learnmaster.tccv2.model.TokenRecuperacao;
import com.example.learnmaster.tccv2.model.Usuario;
import com.example.learnmaster.tccv2.repository.TokenRecuperacaoRepository;
import com.example.learnmaster.tccv2.repository.UsuarioRepository;
import com.example.learnmaster.tccv2.security.LoginRateLimiter;
import com.example.learnmaster.tccv2.security.SenhaPolicy;
import com.example.learnmaster.tccv2.security.Tokens;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class AuthService {

    private static final String CREDENCIAIS_INVALIDAS = "E-mail ou senha incorretos.";
    private static final Duration VALIDADE_RECUPERACAO = Duration.ofMinutes(30);
    private static final Duration JANELA_RECUPERACAO = Duration.ofMinutes(15);
    private static final int MAX_PEDIDOS_RECUPERACAO = 3;

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacaoRepository tokenRecuperacaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter rateLimiter;
    private final SessaoService sessaoService;
    private final EmailService emailService;
    private final String frontendUrl;
    // Hash usado quando o e-mail nao existe, para o login levar o mesmo tempo nos dois casos
    private final String hashFicticio;

    public AuthService(UsuarioRepository usuarioRepository,
                       TokenRecuperacaoRepository tokenRecuperacaoRepository,
                       PasswordEncoder passwordEncoder,
                       LoginRateLimiter rateLimiter,
                       SessaoService sessaoService,
                       EmailService emailService,
                       @Value("${app.frontend-url}") String frontendUrl) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRecuperacaoRepository = tokenRecuperacaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiter = rateLimiter;
        this.sessaoService = sessaoService;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
        this.hashFicticio = passwordEncoder.encode("senha-ficticia-para-tempo-constante");
    }

    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public Usuario cadastrar(CadastroRequest req) {
        String email = normalizarEmail(req.email());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Já existe uma conta com este e-mail.");
        }
        SenhaPolicy.validar(req.senha());

        Usuario usuario = new Usuario();
        usuario.setNome(req.nome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(req.senha()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario autenticar(String emailInformado, String senha) {
        String email = normalizarEmail(emailInformado);
        if (rateLimiter.bloqueado(email)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas de login. Tente novamente em alguns minutos.");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
        if (usuario == null) {
            passwordEncoder.matches(senha, hashFicticio);
            rateLimiter.registrarFalha(email);
            throw new ApiException(HttpStatus.UNAUTHORIZED, CREDENCIAIS_INVALIDAS);
        }
        if (!senhaConfere(usuario, senha)) {
            rateLimiter.registrarFalha(email);
            throw new ApiException(HttpStatus.UNAUTHORIZED, CREDENCIAIS_INVALIDAS);
        }
        rateLimiter.limpar(email);
        return usuario;
    }

    /*
     * Confere a senha do usuario. Contas antigas ainda tem a senha em texto puro no banco:
     * se a senha informada for igual, ela e convertida para BCrypt na hora (migracao gradual).
     * Deve ser chamado dentro de uma transacao, para a conversao ser gravada.
     */
    public boolean senhaConfere(Usuario usuario, String senha) {
        String salva = usuario.getSenha();
        if (salva == null || senha == null) return false;

        if (salva.startsWith("$2")) {
            return passwordEncoder.matches(senha, salva);
        }

        boolean igual = MessageDigest.isEqual(
                salva.getBytes(StandardCharsets.UTF_8), senha.getBytes(StandardCharsets.UTF_8));
        if (igual) {
            usuario.setSenha(passwordEncoder.encode(senha));
            usuarioRepository.save(usuario);
        }
        return igual;
    }

    // Sempre termina sem erro, exista ou nao o e-mail (a tela mostra a mesma mensagem)
    @Transactional
    public void solicitarRecuperacao(String emailInformado) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(normalizarEmail(emailInformado)).orElse(null);
        if (usuario == null) return;

        LocalDateTime agora = SessaoService.agoraUtc();
        long recentes = tokenRecuperacaoRepository.countByUsuarioIdAndCriadoEmAfter(
                usuario.getId(), agora.minus(JANELA_RECUPERACAO));
        if (recentes >= MAX_PEDIDOS_RECUPERACAO) return;

        tokenRecuperacaoRepository.invalidarPendentes(usuario.getId(), agora);

        String token = Tokens.gerar();
        TokenRecuperacao registro = new TokenRecuperacao();
        registro.setUsuarioId(usuario.getId());
        registro.setTokenHash(Tokens.hash(token));
        registro.setExpiraEm(agora.plus(VALIDADE_RECUPERACAO));
        tokenRecuperacaoRepository.save(registro);

        String link = frontendUrl + "/redefinir-senha?token=" + token;
        emailService.enviarRecuperacao(usuario.getEmail(), usuario.getNome(), link);
    }

    // Troca a senha, marca o token como usado e derruba todas as sessoes, tudo na mesma transacao
    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        TokenRecuperacao registro = tokenRecuperacaoRepository.findByTokenHash(Tokens.hash(token)).orElse(null);
        LocalDateTime agora = SessaoService.agoraUtc();
        if (registro == null || registro.getUsadoEm() != null || registro.getExpiraEm().isBefore(agora)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Link inválido ou expirado. Peça um novo.");
        }
        SenhaPolicy.validar(novaSenha);

        Usuario usuario = usuarioRepository.findById(registro.getUsuarioId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Link inválido ou expirado. Peça um novo."));
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        registro.setUsadoEm(agora);
        sessaoService.revogarTodas(usuario.getId());
        rateLimiter.limpar(normalizarEmail(usuario.getEmail()));
    }
}
