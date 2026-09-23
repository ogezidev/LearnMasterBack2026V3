package com.example.learnmaster.tccv2.repository;

import com.example.learnmaster.tccv2.model.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// O dono de um card e definido pela hierarquia (Flashcard -> Deck -> MainDeck -> Usuario)
public interface FlashcardRepository extends JpaRepository<Flashcard, Integer> {

    @Query("select f from Flashcard f where f.deckId in (select d.id from Deck d where d.mainDeckId in "
            + "(select m.id from MainDeck m where m.usuarioId = :usuarioId)) order by f.id")
    List<Flashcard> doUsuario(@Param("usuarioId") Integer usuarioId);

    @Query("select f from Flashcard f where f.id = :id and f.deckId in (select d.id from Deck d where d.mainDeckId in "
            + "(select m.id from MainDeck m where m.usuarioId = :usuarioId))")
    Optional<Flashcard> doUsuario(@Param("id") Integer id, @Param("usuarioId") Integer usuarioId);
}
