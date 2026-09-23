package com.example.learnmaster.tccv2.controller;

import com.example.learnmaster.tccv2.dto.FlashcardRequest;
import com.example.learnmaster.tccv2.exception.ApiException;
import com.example.learnmaster.tccv2.model.Flashcard;
import com.example.learnmaster.tccv2.repository.DeckRepository;
import com.example.learnmaster.tccv2.repository.FlashcardRepository;
import com.example.learnmaster.tccv2.security.UsuarioLogado;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Flashcards: so dentro de um deck do proprio usuario
@RestController
@RequestMapping("/flashcards")
public class FlashcardController {

    private final FlashcardRepository flashcardRepository;
    private final DeckRepository deckRepository;

    public FlashcardController(FlashcardRepository flashcardRepository, DeckRepository deckRepository) {
        this.flashcardRepository = flashcardRepository;
        this.deckRepository = deckRepository;
    }

    @PostMapping
    public Flashcard criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody FlashcardRequest req) {
        Integer usuarioId = UsuarioLogado.id(jwt);
        exigirDeck(req.deckId(), usuarioId);

        Flashcard flashcard = new Flashcard();
        preencher(flashcard, req);
        flashcard.setUsuarioId(usuarioId);
        return flashcardRepository.save(flashcard);
    }

    @GetMapping
    public List<Flashcard> listar(@AuthenticationPrincipal Jwt jwt) {
        return flashcardRepository.doUsuario(UsuarioLogado.id(jwt));
    }

    @GetMapping("/{id}")
    public Flashcard buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id) {
        return doUsuario(jwt, id);
    }

    @PutMapping("/{id}")
    public Flashcard atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id,
                               @Valid @RequestBody FlashcardRequest req) {
        Flashcard flashcard = doUsuario(jwt, id);
        exigirDeck(req.deckId(), UsuarioLogado.id(jwt));
        preencher(flashcard, req);
        return flashcardRepository.save(flashcard);
    }

    @DeleteMapping("/{id}")
    public void deletar(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id) {
        flashcardRepository.delete(doUsuario(jwt, id));
    }

    // "nome" e uma copia da frente mantida por compatibilidade com a tabela atual (sai na Fase 3)
    private void preencher(Flashcard flashcard, FlashcardRequest req) {
        flashcard.setFrente(req.frente());
        flashcard.setVerso(req.verso());
        flashcard.setNome(req.frente());
        flashcard.setDeckId(req.deckId());
    }

    private Flashcard doUsuario(Jwt jwt, Integer id) {
        return flashcardRepository.doUsuario(id, UsuarioLogado.id(jwt)).orElseThrow(ApiException::naoEncontrado);
    }

    private void exigirDeck(Integer deckId, Integer usuarioId) {
        deckRepository.doUsuario(deckId, usuarioId).orElseThrow(ApiException::naoEncontrado);
    }
}
