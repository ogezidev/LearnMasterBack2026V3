package com.example.learnmaster.tccv2.repository;

import com.example.learnmaster.tccv2.model.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// O dono de um deck e definido pela hierarquia (Deck -> MainDeck -> Usuario)
public interface DeckRepository extends JpaRepository<Deck, Integer> {

    @Query("select d from Deck d where d.mainDeckId in "
            + "(select m.id from MainDeck m where m.usuarioId = :usuarioId) order by d.id")
    List<Deck> doUsuario(@Param("usuarioId") Integer usuarioId);

    @Query("select d from Deck d where d.id = :id and d.mainDeckId in "
            + "(select m.id from MainDeck m where m.usuarioId = :usuarioId)")
    Optional<Deck> doUsuario(@Param("id") Integer id, @Param("usuarioId") Integer usuarioId);
}
