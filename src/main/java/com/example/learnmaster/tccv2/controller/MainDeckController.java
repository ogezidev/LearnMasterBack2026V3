package com.example.learnmaster.tccv2.controller;

import com.example.learnmaster.tccv2.dto.MainDeckRequest;
import com.example.learnmaster.tccv2.exception.ApiException;
import com.example.learnmaster.tccv2.model.MainDeck;
import com.example.learnmaster.tccv2.repository.MainDeckRepository;
import com.example.learnmaster.tccv2.security.UsuarioLogado;
import com.example.learnmaster.tccv2.service.HierarquiaService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// LearnDecks: o dono vem sempre do token, nunca do corpo da requisicao
@RestController
@RequestMapping("/maindecks")
public class MainDeckController {

    private final MainDeckRepository mainDeckRepository;
    private final HierarquiaService hierarquiaService;

    public MainDeckController(MainDeckRepository mainDeckRepository, HierarquiaService hierarquiaService) {
        this.mainDeckRepository = mainDeckRepository;
        this.hierarquiaService = hierarquiaService;
    }

    @PostMapping
    public MainDeck criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody MainDeckRequest req) {
        MainDeck mainDeck = new MainDeck();
        mainDeck.setNome(req.nome().trim());
        mainDeck.setUsuarioId(UsuarioLogado.id(jwt));
        return mainDeckRepository.save(mainDeck);
    }

    @GetMapping
    public List<MainDeck> listar(@AuthenticationPrincipal Jwt jwt) {
        return mainDeckRepository.findByUsuarioIdOrderByIdAsc(UsuarioLogado.id(jwt));
    }

    @GetMapping("/{id}")
    public MainDeck buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id) {
        return doUsuario(jwt, id);
    }

    @PutMapping("/{id}")
    public MainDeck atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id,
                              @Valid @RequestBody MainDeckRequest req) {
        MainDeck mainDeck = doUsuario(jwt, id);
        mainDeck.setNome(req.nome().trim());
        return mainDeckRepository.save(mainDeck);
    }

    // Apaga o LearnDeck com todos os decks, cards e avaliacoes dele, numa transacao
    @DeleteMapping("/{id}")
    public void deletar(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id) {
        hierarquiaService.excluirMainDeck(doUsuario(jwt, id));
    }

    private MainDeck doUsuario(Jwt jwt, Integer id) {
        return mainDeckRepository.findByIdAndUsuarioId(id, UsuarioLogado.id(jwt))
                .orElseThrow(ApiException::naoEncontrado);
    }
}
