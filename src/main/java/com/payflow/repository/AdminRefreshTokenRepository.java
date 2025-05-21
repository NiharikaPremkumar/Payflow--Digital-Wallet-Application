package com.payflow.repository;

import com.payflow.model.Admin;
import com.payflow.model.AdminRefreshToken;
import com.payflow.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRefreshTokenRepository extends JpaRepository<AdminRefreshToken, Long> {
    Optional<AdminRefreshToken> findByAdmin(Admin admin);
}


