package com.example.learnmaster.tccv2.service;

import com.example.learnmaster.tccv2.exception.ApiException;
import com.example.learnmaster.tccv2.model.Usuario;
import com.example.learnmaster.tccv2.repository.DeckRepository;
import com.example.learnmaster.tccv2.repository.UsuarioRepository;
import com.example.learnmaster.tccv2.security.SenhaPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Alteracoes da conta do usuario logado
@Service
public class ContaService {

    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;
    private final SessaoService sessaoService;
    private final PasswordEncoder passwordEncoder;
    private final DeckRepository deckRepository;

    public ContaService(UsuarioRepository usuarioRepository, AuthService authService,
                        SessaoService sessaoService, PasswordEncoder passwordEncoder,
                        DeckRepository deckRepository) {
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
        this.sessaoService = sessaoService;
        this.passwordEncoder = passwordEncoder;
        this.deckRepository = deckRepository;
    }

    // Deck aberto ao entrar em Memorizar; so aceita deck do proprio usuario
    @Transactional
    public Usuario definirUltimoDeck(Integer id, Integer deckId) {
        Usuario usuario = buscar(id);
        if (deckId != null) {
            deckRepository.doUsuario(deckId, id).orElseThrow(ApiException::naoEncontrado);
        }
        usuario.setUltimoDeckId(deckId);
        return usuario;
    }

    public Usuario buscar(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Sessão expirada. Entre novamente."));
    }

    @Transactional
    public Usuario atualizarNome(Integer id, String nome) {
        Usuario usuario = buscar(id);
        usuario.setNome(nome.trim());
        return usuario;
    }

    @Transactional
    public Usuario alterarEmail(Integer id, String novoEmail, String senhaAtual) {
        Usuario usuario = buscar(id);
        exigirSenhaAtual(usuario, senhaAtual);

        String email = AuthService.normalizarEmail(novoEmail);
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Este e-mail já está em uso.");
        }
        usuario.setEmail(email);
        return usuario;
    }

    // Troca a senha e derruba todas as sessoes; o controller abre uma sessao nova para este navegador
    @Transactional
    public Usuario alterarSenha(Integer id, String senhaAtual, String novaSenha) {
        Usuario usuario = buscar(id);
        exigirSenhaAtual(usuario, senhaAtual);
        SenhaPolicy.validar(novaSenha);

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        sessaoService.revogarTodas(id);
        return usuario;
    }

    private void exigirSenhaAtual(Usuario usuario, String senhaAtual) {
        if (!authService.senhaConfere(usuario, senhaAtual)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Senha atual incorreta.");
        }
    }
}
