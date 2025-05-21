package com.payflow.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    private BigDecimal balance = BigDecimal.valueOf(0.00);

    private String currency = "INR";

    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();
}

