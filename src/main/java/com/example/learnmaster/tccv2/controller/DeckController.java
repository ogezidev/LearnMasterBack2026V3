package com.example.learnmaster.tccv2.controller;

import com.example.learnmaster.tccv2.dto.DeckRequest;
import com.example.learnmaster.tccv2.exception.ApiException;
import com.example.learnmaster.tccv2.model.Deck;
import com.example.learnmaster.tccv2.repository.DeckRepository;
import com.example.learnmaster.tccv2.repository.MainDeckRepository;
import com.example.learnmaster.tccv2.security.UsuarioLogado;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Decks: so dentro de um LearnDeck do proprio usuario
@RestController
@RequestMapping("/decks")
public class DeckController {

    private final DeckRepository deckRepository;
    private final MainDeckRepository mainDeckRepository;

    public DeckController(DeckRepository deckRepository, MainDeckRepository mainDeckRepository) {
        this.deckRepository = deckRepository;
        this.mainDeckRepository = mainDeckRepository;
    }

    @PostMapping
    public Deck criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DeckRequest req) {
        Integer usuarioId = UsuarioLogado.id(jwt);
        exigirMainDeck(req.mainDeckId(), usuarioId);

        Deck deck = new Deck();
        deck.setNome(req.nome().trim());
        deck.setMainDeckId(req.mainDeckId());
        deck.setUsuarioId(usuarioId);
        return deckRepository.save(deck);
    }

    @GetMapping
    public List<Deck> listar(@AuthenticationPrincipal Jwt jwt) {
        return deckRepository.doUsuario(UsuarioLogado.id(jwt));
    }

    @GetMapping("/{id}")
    public Deck buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id) {
        return doUsuario(jwt, id);
    }

    @PutMapping("/{id}")
    public Deck atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id,
                          @Valid @RequestBody DeckRequest req) {
        Integer usuarioId = UsuarioLogado.id(jwt);
        Deck deck = doUsuario(jwt, id);
        exigirMainDeck(req.mainDeckId(), usuarioId);
        deck.setNome(req.nome().trim());
        deck.setMainDeckId(req.mainDeckId());
        return deckRepository.save(deck);
    }

    @DeleteMapping("/{id}")
    public void deletar(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id) {
        deckRepository.delete(doUsuario(jwt, id));
    }

    private Deck doUsuario(Jwt jwt, Integer id) {
        return deckRepository.doUsuario(id, UsuarioLogado.id(jwt)).orElseThrow(ApiException::naoEncontrado);
    }

    private void exigirMainDeck(Integer mainDeckId, Integer usuarioId) {
        mainDeckRepository.findByIdAndUsuarioId(mainDeckId, usuarioId).orElseThrow(ApiException::naoEncontrado);
    }
}
