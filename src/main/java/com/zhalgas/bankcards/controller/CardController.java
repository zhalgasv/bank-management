package com.zhalgas.bankcards.controller;

import com.zhalgas.bankcards.dto.CardResponse;
import com.zhalgas.bankcards.service.CardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.zhalgas.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/my")
    public Page<CardResponse> findMyCards(
        Authentication authentication,
    @RequestParam(required = false) CardStatus status,
    Pageable pageable
    ) {
        return cardService.findMyCards(
                authentication.getName(),
                status,
                pageable
        );
    }
}
