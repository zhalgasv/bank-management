package com.zhalgas.bankcards.repository;

import com.zhalgas.bankcards.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByIdAndOwnerUsername(Long id, String username);

    boolean existsByNumberHash(String numberHash);
}
