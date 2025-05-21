package com.payflow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class AdminRefreshToken {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "admin_id", nullable = false)
        private Admin admin;

        @Column(length = 500, nullable = false)
        private String refreshToken;

        @Column(name = "created_at")
        private LocalDateTime createdAt = LocalDateTime.now();

        @Column(name = "expiry_date")
        private LocalDateTime expiryDate;
    }


// Getters and setters

