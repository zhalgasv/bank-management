package com.zhalgas.bankcards.repository;

import com.zhalgas.bankcards.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import com.zhalgas.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByIdAndOwnerUsername(Long id, String username);

    boolean existsByNumberHash(String numberHash);

    Page<Card> findAllByOwnerUsername(String username, Pageable pageable);

    Page<Card> findAllByOwnerUsernameAndStatus(
            String username,
            CardStatus status,
            Pageable pageable
    );

    Page<Card> findAllByStatus(CardStatus status, Pageable pageable);
}
