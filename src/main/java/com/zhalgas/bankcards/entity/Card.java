package com.zhalgas.bankcards.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;


@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "encrypted_number"  ,nullable = false, length = 255)
    private String encryptedCardNumber;

    @Column(name = "number_hash", nullable = false, unique = true, length = 64)
    private String numberHash;

    @Column(name = "last_four", nullable = false, length = 4)
    private String lastFour;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "expiry_date", nullable = false, length = 7)
    private YearMonth expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CardStatus status = CardStatus.ACTIVE;

    @Column(name = "balance",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
