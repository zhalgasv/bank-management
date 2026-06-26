package com.zhalgas.bankcards.service;

import com.zhalgas.bankcards.dto.CardResponse;
import com.zhalgas.bankcards.dto.CreateCardRequest;
import com.zhalgas.bankcards.entity.Card;
import com.zhalgas.bankcards.entity.CardStatus;
import com.zhalgas.bankcards.entity.User;
import com.zhalgas.bankcards.exception.UserNotFoundException;
import com.zhalgas.bankcards.repository.CardRepository;
import com.zhalgas.bankcards.repository.UserRepository;
import com.zhalgas.bankcards.util.CardDataProtector;
import com.zhalgas.bankcards.util.CardNumberGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.zhalgas.bankcards.exception.CardNotFoundException;
import com.zhalgas.bankcards.dto.TransferRequest;
import com.zhalgas.bankcards.exception.InsufficientFundsException;
import com.zhalgas.bankcards.exception.InvalidTransferException;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardNumberGenerator  cardNumberGenerator;
    private final CardDataProtector dataProtector;

    public CardService(
            CardRepository cardRepository,
            UserRepository userRepository,
            CardNumberGenerator cardNumberGenerator,
            CardDataProtector dataProtector
    ) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cardNumberGenerator = cardNumberGenerator;
        this.dataProtector = dataProtector;
    }


    private User findOwner(Long ownerId) {

        User user = userRepository.findById(ownerId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found: " + ownerId
                        )
                );
        return user;
    }

    private String generateUniqueNumber() {
        String number;
        String hash;

        do {
           number = cardNumberGenerator.generate();
           hash = dataProtector.hash(number);
        } while (
                cardRepository.existsByNumberHash(hash)
        );
          return number;
    }

   private CardResponse toResponse(Card card) {
        return new CardResponse(
                card.getId(),
                dataProtector.mask(card.getLastFour()),
                card.getOwner().getId(),
                card.getOwner().getUsername(),
                card.getExpiryDate(),
                card.getStatus(),
                card.getBalance(),
                card.getCreatedAt()
        );
   }

   @Transactional
    public CardResponse create(CreateCardRequest request) {
        User owner = findOwner(request.ownerId());
        String number = generateUniqueNumber();

        Card card = new Card();

        card.setEncryptedCardNumber(dataProtector.encrypt(number));
        card.setNumberHash(dataProtector.hash(number));
        card.setLastFour(number.substring(number.length() - 4));
        card.setOwner(owner);
        card.setExpiryDate(request.expiryDate());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(request.initialBalance());

        Card savedCard = cardRepository.save(card);

        return toResponse(savedCard);
   }

   @Transactional(readOnly = true)
    public Page<CardResponse> findMyCards(
            String username,
            CardStatus status,
            Pageable pageable
   ) {
        Page<Card> cards;

        if(status == null) {
            cards = cardRepository.findAllByOwnerUsername(username, pageable);
        } else {
            cards = cardRepository.findAllByOwnerUsernameAndStatus(
                    username,
                    status,
                    pageable
            );
        }
        return cards.map(this::toResponse);
   }

   private Card findCard(Long cardId) {
       return cardRepository.findById(cardId)
               .orElseThrow(() ->
                       new CardNotFoundException(
                               "Card not found: " + cardId
                       )
               );
   }

   @Transactional
    public CardResponse block(Long cardId) {
        Card card = findCard(cardId);
        card.setStatus(CardStatus.BLOCKED);
        return toResponse(card);
   }

   @Transactional
    public CardResponse activate(Long cardId) {
        Card card = findCard(cardId);
        card.setStatus(CardStatus.ACTIVE);
        return toResponse(card);
   }

   @Transactional
    public void delete(Long cardId) {
        Card card = findCard(cardId);
        cardRepository.delete(card);
   }

   @Transactional(readOnly = true)
    public Page<CardResponse> findAllCards(CardStatus status, Pageable pageable) {
        Page<Card> cards;

        if(status == null) {
            cards = cardRepository.findAll(pageable);
        } else {
            cards = cardRepository.findAllByStatus(status, pageable);
        }

        return cards.map(this::toResponse);
   }

   private Card findUserCard(Long cardId, String username) {
        return cardRepository.findByIdAndOwnerUsername(cardId, username)
                .orElseThrow(() ->
                        new CardNotFoundException(
                                "Card not found: " + cardId
                        )
                );
   }

   @Transactional
    public CardResponse requestBlock(Long cardId, String username) {
        Card card = findUserCard(cardId, username);
        card.setStatus(CardStatus.BLOCKED);
        return toResponse(card);
   }

   @Transactional
    public void transfer(TransferRequest request, String username) {
        if (request.fromCardId().equals(request.toCardId())) {
            throw new InvalidTransferException(
                    "Cannot transfer money to the same card"
            );
        }

        Card fromCard = findUserCard(request.fromCardId(), username);
        Card toCard = findUserCard(request.toCardId(), username);

        if(fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidTransferException(
                    "Source card is not active"
            );
        }
        if(toCard.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidTransferException(
                    "Target card is not active"
            );
        }

        if(fromCard.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds"
            );
        }

        fromCard.setBalance(
                fromCard.getBalance().subtract(request.amount())
        );

        toCard.setBalance(
                toCard.getBalance().add(request.amount())
        );
   }
}
