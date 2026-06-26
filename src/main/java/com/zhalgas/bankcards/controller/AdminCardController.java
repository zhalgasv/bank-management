package com.zhalgas.bankcards.controller;

import com.zhalgas.bankcards.dto.CardResponse;
import com.zhalgas.bankcards.dto.CreateCardRequest;
import com.zhalgas.bankcards.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.zhalgas.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


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

    @PatchMapping("/{id}/block")
    public CardResponse block(@PathVariable Long id) {
        return cardService.block(id);
    }

    @PatchMapping("/{id}/activate")
    public CardResponse activate(@PathVariable Long id) {
        return cardService.activate(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        cardService.delete(id);
    }

    @GetMapping
    public Page<CardResponse> findAllCards(
            @RequestParam(required = false) CardStatus status,
            Pageable pageable
    ) {
        return cardService.findAllCards(status, pageable);
    }
}
