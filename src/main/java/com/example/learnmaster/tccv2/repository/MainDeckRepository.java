package com.example.learnmaster.tccv2.repository;

import com.example.learnmaster.tccv2.model.MainDeck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MainDeckRepository extends JpaRepository<MainDeck, Integer> {

    List<MainDeck> findByUsuarioIdOrderByIdAsc(Integer usuarioId);

    Optional<MainDeck> findByIdAndUsuarioId(Integer id, Integer usuarioId);
}
