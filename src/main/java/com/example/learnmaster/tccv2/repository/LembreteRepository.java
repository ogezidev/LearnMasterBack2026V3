package com.example.learnmaster.tccv2.repository;

import com.example.learnmaster.tccv2.model.Lembrete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LembreteRepository extends JpaRepository<Lembrete, Integer> {

    List<Lembrete> findByUsuarioIdOrderByHorarioAscIdAsc(Integer usuarioId);

    Optional<Lembrete> findByIdAndUsuarioId(Integer id, Integer usuarioId);

    long countByUsuarioId(Integer usuarioId);

    // Deck excluido: o lembrete continua, so sem o deck
    @Modifying
    @Query("update Lembrete l set l.deckId = null where l.deckId in :deckIds")
    int soltarDosDecks(@Param("deckIds") Collection<Integer> deckIds);
}
