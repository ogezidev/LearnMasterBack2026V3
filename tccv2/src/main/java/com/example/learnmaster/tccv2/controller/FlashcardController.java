package com.example.learnmaster.tccv2.controller;

import com.example.learnmaster.tccv2.model.Flashcard;
import com.example.learnmaster.tccv2.repository.FlashcardRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flashcards")
public class FlashcardController {

    private final FlashcardRepository flashcardRepository;

    public FlashcardController(FlashcardRepository flashcardRepository) {
        this.flashcardRepository = flashcardRepository;
    }

    @PostMapping
    public Flashcard criar(@RequestBody Flashcard flashcard){
        return flashcardRepository.save(flashcard);
    }

    @GetMapping
    public List<Flashcard> listar(){
        return flashcardRepository.findAll();
    }

    @GetMapping("/{id}")
    public Flashcard buscar(@PathVariable Integer id){
        return flashcardRepository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public Flashcard atualizar(@PathVariable Integer id, @RequestBody Flashcard flashcard){
        flashcard.setId(id);
        return flashcardRepository.save(flashcard);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id){
        flashcardRepository.deleteById(id);
    }
}