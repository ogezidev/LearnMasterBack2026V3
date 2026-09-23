package com.example.learnmaster.tccv2.controller;

import com.example.learnmaster.tccv2.model.Usuario;
import com.example.learnmaster.tccv2.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody Usuario loginData) {

        return usuarioRepository.findByEmail(loginData.getEmail())
                .filter(usuario -> passwordEncoder.matches(loginData.getSenha(), usuario.getSenha()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    // CREATE
    @PostMapping
    public Usuario criar(@RequestBody Usuario usuario){
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    // READ
    @GetMapping
    public List<Usuario> listar(){
        return usuarioRepository.findAll();
    }

    @GetMapping("/{id}")
    public Usuario buscar(@PathVariable Integer id){
        return usuarioRepository.findById(id).orElse(null);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Usuario atualizar(@PathVariable Integer id, @RequestBody Usuario usuario){
        usuario.setId(id);
        return usuarioRepository.save(usuario);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id){
        usuarioRepository.deleteById(id);
    }
}