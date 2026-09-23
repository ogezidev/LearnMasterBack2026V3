package com.example.learnmaster.tccv2.controller;

import com.example.learnmaster.tccv2.dto.FlashcardLoteRequest;
import com.example.learnmaster.tccv2.dto.FlashcardRequest;
import com.example.learnmaster.tccv2.exception.ApiException;
import com.example.learnmaster.tccv2.model.Flashcard;
import com.example.learnmaster.tccv2.repository.DeckRepository;
import com.example.learnmaster.tccv2.repository.FlashcardRepository;
import com.example.learnmaster.tccv2.security.UsuarioLogado;
import com.example.learnmaster.tccv2.service.HierarquiaService;
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
    private final HierarquiaService hierarquiaService;

    public FlashcardController(FlashcardRepository flashcardRepository, DeckRepository deckRepository,
                               HierarquiaService hierarquiaService) {
        this.flashcardRepository = flashcardRepository;
        this.deckRepository = deckRepository;
        this.hierarquiaService = hierarquiaService;
    }

    @PostMapping
    public Flashcard criar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody FlashcardRequest req) {
        exigirDeck(req.deckId(), UsuarioLogado.id(jwt));

        Flashcard flashcard = new Flashcard();
        preencher(flashcard, req);
        return flashcardRepository.save(flashcard);
    }

    // Tela Criar: ate 5 cards de uma vez, todos salvos ou nenhum
    @PostMapping("/lote")
    public List<Flashcard> criarLote(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody FlashcardLoteRequest req) {
        exigirDeck(req.deckId(), UsuarioLogado.id(jwt));
        return hierarquiaService.criarCards(req.deckId(), req.cards());
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

    // Apaga o card e as avaliacoes dele, numa transacao
    @DeleteMapping("/{id}")
    public void deletar(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id) {
        hierarquiaService.excluirFlashcard(doUsuario(jwt, id));
    }

    private void preencher(Flashcard flashcard, FlashcardRequest req) {
        flashcard.setFrente(req.frente());
        flashcard.setVerso(req.verso());
        flashcard.setDeckId(req.deckId());
    }

    private Flashcard doUsuario(Jwt jwt, Integer id) {
        return flashcardRepository.doUsuario(id, UsuarioLogado.id(jwt)).orElseThrow(ApiException::naoEncontrado);
    }

    private void exigirDeck(Integer deckId, Integer usuarioId) {
        deckRepository.doUsuario(deckId, usuarioId).orElseThrow(ApiException::naoEncontrado);
    }
}
