package com.example.learnmaster.tccv2.repository;

import com.example.learnmaster.tccv2.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Integer> {

    // Avaliacao nao tem cascata no banco: precisa sair antes dos cards
    @Modifying
    @Query("delete from Avaliacao a where a.flashcardId in :flashcardIds")
    int apagarDosCards(@Param("flashcardIds") Collection<Integer> flashcardIds);

    @Modifying
    @Query("delete from Avaliacao a where a.flashcardId in (select f.id from Flashcard f where f.deckId in :deckIds)")
    int apagarDosDecks(@Param("deckIds") Collection<Integer> deckIds);
}
