package com.example.learnmaster.tccv2.controller;

import com.example.learnmaster.tccv2.model.MainDeck;
import com.example.learnmaster.tccv2.repository.MainDeckRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/maindecks")
public class MainDeckController {

    private final MainDeckRepository mainDeckRepository;

    public MainDeckController(MainDeckRepository mainDeckRepository) {
        this.mainDeckRepository = mainDeckRepository;
    }

    @PostMapping
    public MainDeck Scriar(@RequestBody MainDeck mainDeck){
        return mainDeckRepository.save(mainDeck);
    }

    @GetMapping
    public List<MainDeck> listar(){
        return mainDeckRepository.findAll();
    }

    @GetMapping("/{id}")
    public MainDeck buscar(@PathVariable Integer id){
        return mainDeckRepository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public MainDeck atualizar(@PathVariable Integer id, @RequestBody MainDeck mainDeck){
        mainDeck.setId(id);
        return mainDeckRepository.save(mainDeck);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id){
        mainDeckRepository.deleteById(id);
    }
}