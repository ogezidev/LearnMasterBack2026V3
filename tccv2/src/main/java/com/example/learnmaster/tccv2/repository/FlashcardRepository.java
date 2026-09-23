package com.example.learnmaster.tccv2.repository;

import com.example.learnmaster.tccv2.model.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardRepository extends JpaRepository<Flashcard, Integer> {
}