package com.example.learnmaster.tccv2.dto;

import com.example.learnmaster.tccv2.model.Usuario;

// Dados do usuario devolvidos pela API: nunca inclui a senha
public record UsuarioResponse(
        Integer id,
        String nome,
        String email,
        boolean modoNoturno,
        boolean fonteDislexia,
        boolean tutorialConcluido,
        Integer ultimoDeckId) {

    public static UsuarioResponse of(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.isModoNoturno(),
                u.isFonteDislexia(), u.isTutorialConcluido(), u.getUltimoDeckId());
    }
}
