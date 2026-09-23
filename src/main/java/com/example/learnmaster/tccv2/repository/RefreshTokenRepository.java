package com.example.learnmaster.tccv2.repository;

import com.example.learnmaster.tccv2.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
}
