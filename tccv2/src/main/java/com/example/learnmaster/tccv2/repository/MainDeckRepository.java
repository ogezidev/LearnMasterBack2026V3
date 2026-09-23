package com.example.learnmaster.tccv2.repository;

import com.example.learnmaster.tccv2.model.MainDeck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MainDeckRepository extends JpaRepository<MainDeck, Integer> {
}