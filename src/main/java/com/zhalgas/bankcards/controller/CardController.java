package com.zhalgas.bankcards.controller;

import com.zhalgas.bankcards.dto.CardResponse;
import com.zhalgas.bankcards.service.CardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/my")
    public List<CardResponse> findMyCards(Authentication authentication) {
        return cardService.findMyCards(authentication.getName());
    }
}
