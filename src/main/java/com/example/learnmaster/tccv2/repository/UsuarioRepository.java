package com.example.learnmaster.tccv2.repository;

import com.example.learnmaster.tccv2.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);

    // ultimo_deck_id nao tem cascata no banco: zera antes de apagar os decks
    @Modifying
    @Query("update Usuario u set u.ultimoDeckId = null where u.ultimoDeckId in :deckIds")
    int esquecerUltimoDeck(@Param("deckIds") Collection<Integer> deckIds);
}
