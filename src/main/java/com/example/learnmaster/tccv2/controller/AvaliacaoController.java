package com.example.learnmaster.tccv2.controller;

import com.example.learnmaster.tccv2.dto.AvaliacaoRequest;
import com.example.learnmaster.tccv2.exception.ApiException;
import com.example.learnmaster.tccv2.model.Avaliacao;
import com.example.learnmaster.tccv2.repository.AvaliacaoRepository;
import com.example.learnmaster.tccv2.repository.FlashcardRepository;
import com.example.learnmaster.tccv2.security.UsuarioLogado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoRepository avaliacaoRepository;
    private final FlashcardRepository flashcardRepository;

    public AvaliacaoController(AvaliacaoRepository avaliacaoRepository, FlashcardRepository flashcardRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.flashcardRepository = flashcardRepository;
    }

    // Cada avaliacao vira uma linha nova no historico; a atual e sempre a mais recente
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> avaliar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AvaliacaoRequest req) {
        Integer usuarioId = UsuarioLogado.id(jwt);
        flashcardRepository.doUsuario(req.flashcardId(), usuarioId).orElseThrow(ApiException::naoEncontrado);

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuarioId(usuarioId);
        avaliacao.setFlashcardId(req.flashcardId());
        avaliacao.setNivel(req.nivel());
        avaliacaoRepository.save(avaliacao);
        return Map.of("flashcardId", req.flashcardId(), "nivel", req.nivel());
    }

    // { flashcardId: "dificil" | "bom" | "facil" } com a avaliacao atual (a mais recente) de cada card
    @GetMapping("/atuais")
    public Map<Integer, String> atuais(@AuthenticationPrincipal Jwt jwt) {
        Map<Integer, String> atuais = new HashMap<>();
        avaliacaoRepository.atuaisDoUsuario(UsuarioLogado.id(jwt))
                .forEach(a -> atuais.put(a.getFlashcardId(), a.getNivel()));
        return atuais;
    }
}
