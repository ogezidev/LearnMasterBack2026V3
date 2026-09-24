package com.example.learnmaster.tccv2.controller;

import com.example.learnmaster.tccv2.model.Avaliacao;
import com.example.learnmaster.tccv2.repository.AvaliacaoRepository;
import com.example.learnmaster.tccv2.security.UsuarioLogado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoRepository avaliacaoRepository;

    public AvaliacaoController(AvaliacaoRepository avaliacaoRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
    }

    // { flashcardId: "dificil" | "bom" | "facil" } com a avaliacao atual (a mais recente) de cada card
    @GetMapping("/atuais")
    public Map<Integer, String> atuais(@AuthenticationPrincipal Jwt jwt) {
        Map<Integer, String> atuais = new HashMap<>();
        avaliacaoRepository.atuaisDoUsuario(UsuarioLogado.id(jwt)).stream()
                .sorted(Comparator.comparing(Avaliacao::getId))
                .forEach(a -> atuais.put(a.getFlashcardId(), a.getNivel()));
        return atuais;
    }
}
