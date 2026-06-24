package com.zhalgas.bankcards.controller;

import com.zhalgas.bankcards.dto.CardResponse;
import com.zhalgas.bankcards.dto.CreateCardRequest;
import com.zhalgas.bankcards.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin/cards")
public class AdminCardController {

    private final CardService cardService;

    public AdminCardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponse create(
            @Valid @RequestBody CreateCardRequest request) {
        return cardService.create(request);
    }
}
