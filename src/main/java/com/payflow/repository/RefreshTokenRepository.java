package com.payflow.repository;

import com.payflow.model.RefreshToken;
import com.payflow.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    @Transactional
    void deleteByToken(String token);

}
