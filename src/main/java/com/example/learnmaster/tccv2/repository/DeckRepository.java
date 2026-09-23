package com.example.learnmaster.tccv2.repository;

import com.example.learnmaster.tccv2.model.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeckRepository extends JpaRepository<Deck, Integer> {
}