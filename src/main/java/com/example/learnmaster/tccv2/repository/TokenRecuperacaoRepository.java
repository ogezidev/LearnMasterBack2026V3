package com.example.learnmaster.tccv2.repository;

import com.example.learnmaster.tccv2.model.TokenRecuperacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRecuperacaoRepository extends JpaRepository<TokenRecuperacao, Integer> {

    Optional<TokenRecuperacao> findByTokenHash(String tokenHash);

    long countByUsuarioIdAndCriadoEmAfter(Integer usuarioId, LocalDateTime desde);

    // Um link novo invalida os anteriores ainda nao usados
    @Modifying
    @Query("update TokenRecuperacao t set t.usadoEm = :agora where t.usuarioId = :usuarioId and t.usadoEm is null")
    int invalidarPendentes(@Param("usuarioId") Integer usuarioId, @Param("agora") LocalDateTime agora);
}
