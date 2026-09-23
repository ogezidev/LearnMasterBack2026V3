package com.example.learnmaster.tccv2.controller;


import com.example.learnmaster.tccv2.model.Deck;
import com.example.learnmaster.tccv2.repository.DeckRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.learnmaster.tccv2.model.Deck;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/decks")
public class DeckController {

    private final DeckRepository deckRepository;

    public DeckController(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    @PostMapping
    public Deck criar(@RequestBody Deck deck){
        return deckRepository.save(deck);
    }

    @GetMapping
    public List<Deck> listar(){
        return deckRepository.findAll();
    }

    @GetMapping("/{id}")
    public Deck buscar(@PathVariable Integer id){
        return deckRepository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public Deck atualizar(@PathVariable Integer id, @RequestBody Deck deck){
        deck.setId(id);
        return deckRepository.save(deck);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id){
        deckRepository.deleteById(id);
    }
}