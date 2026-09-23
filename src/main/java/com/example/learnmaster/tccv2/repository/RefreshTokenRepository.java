package com.example.learnmaster.tccv2.repository;

import com.example.learnmaster.tccv2.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken r set r.revogadoEm = :agora where r.usuarioId = :usuarioId and r.revogadoEm is null")
    int revogarTodos(@Param("usuarioId") Integer usuarioId, @Param("agora") LocalDateTime agora);
}
