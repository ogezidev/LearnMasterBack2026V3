package com.example.learnmaster.tccv2.service;

import com.example.learnmaster.tccv2.dto.FlashcardLoteRequest;
import com.example.learnmaster.tccv2.model.Deck;
import com.example.learnmaster.tccv2.model.Flashcard;
import com.example.learnmaster.tccv2.model.MainDeck;
import com.example.learnmaster.tccv2.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * Operacoes que mexem em varias tabelas da hierarquia LearnDeck -> Deck -> Flashcard.
 * Cada metodo roda numa transacao: ou tudo e gravado, ou nada.
 *
 * A exclusao apaga explicitamente cada nivel (em vez de depender so do ON DELETE CASCADE)
 * porque Avaliacao e Usuario.ultimo_deck_id nao podem ter cascata no SQL Server.
 * A posse dos itens ja foi conferida no controller.
 */
@Service
public class HierarquiaService {

    private final MainDeckRepository mainDeckRepository;
    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public HierarquiaService(MainDeckRepository mainDeckRepository, DeckRepository deckRepository,
                             FlashcardRepository flashcardRepository, AvaliacaoRepository avaliacaoRepository,
                             UsuarioRepository usuarioRepository) {
        this.mainDeckRepository = mainDeckRepository;
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void excluirMainDeck(MainDeck mainDeck) {
        excluirDecks(deckRepository.idsDoMainDeck(mainDeck.getId()));
        mainDeckRepository.delete(mainDeck);
    }

    @Transactional
    public void excluirDeck(Deck deck) {
        excluirDecks(List.of(deck.getId()));
    }

    @Transactional
    public void excluirFlashcard(Flashcard flashcard) {
        avaliacaoRepository.apagarDosCards(List.of(flashcard.getId()));
        flashcardRepository.delete(flashcard);
    }

    // Ou todos os cards do lote sao criados, ou nenhum
    @Transactional
    public List<Flashcard> criarCards(Integer deckId, List<FlashcardLoteRequest.Conteudo> cards) {
        List<Flashcard> novos = cards.stream().map(c -> {
            Flashcard f = new Flashcard();
            f.setDeckId(deckId);
            f.setFrente(c.frente());
            f.setVerso(c.verso());
            return f;
        }).toList();
        return flashcardRepository.saveAll(novos);
    }

    private void excluirDecks(List<Integer> deckIds) {
        if (deckIds.isEmpty()) return;
        avaliacaoRepository.apagarDosDecks(deckIds);
        usuarioRepository.esquecerUltimoDeck(deckIds);
        flashcardRepository.apagarDosDecks(deckIds);
        deckRepository.apagar(deckIds);
    }
}
