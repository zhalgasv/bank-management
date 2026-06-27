package com.zhalgas.bankcards.service;

import com.zhalgas.bankcards.dto.TransferRequest;
import com.zhalgas.bankcards.entity.Card;
import com.zhalgas.bankcards.entity.CardStatus;
import com.zhalgas.bankcards.entity.Role;
import com.zhalgas.bankcards.entity.User;
import com.zhalgas.bankcards.exception.InsufficientFundsException;
import com.zhalgas.bankcards.exception.InvalidTransferException;
import com.zhalgas.bankcards.repository.CardRepository;
import com.zhalgas.bankcards.repository.UserRepository;
import com.zhalgas.bankcards.util.CardDataProtector;
import com.zhalgas.bankcards.util.CardNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CardServiceTransferTest {

    private CardRepository cardRepository;
    private CardService cardService;

    @BeforeEach
    void setUp() {
        cardRepository = mock(CardRepository.class);

        UserRepository userRepository = mock(UserRepository.class);
        CardNumberGenerator cardNumberGenerator = mock(CardNumberGenerator.class);
        CardDataProtector dataProtector = mock(CardDataProtector.class);

        cardService = new CardService(
                cardRepository,
                userRepository,
                cardNumberGenerator,
                dataProtector
        );
    }

    @Test
    void transferMovesMoneyBetweenUserCards() {
        Card fromCard = createCard(1L, new BigDecimal("100.00"));
        Card toCard = createCard(2L, new BigDecimal("20.00"));

        when(cardRepository.findByIdAndOwnerUsername(1L, "adil"))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerUsername(2L, "adil"))
                .thenReturn(Optional.of(toCard));

        TransferRequest request = new TransferRequest(
                1L,
                2L,
                new BigDecimal("30.00")
        );

        cardService.transfer(request, "adil");

        assertThat(fromCard.getBalance())
                .isEqualByComparingTo("70.00");
        assertThat(toCard.getBalance())
                .isEqualByComparingTo("50.00");
    }

    @Test
    void transferThrowsExceptionWhenNotEnoughMoney() {
        Card fromCard = createCard(1L, new BigDecimal("10.00"));
        Card toCard = createCard(2L, new BigDecimal("20.00"));

        when(cardRepository.findByIdAndOwnerUsername(1L, "adil"))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerUsername(2L, "adil"))
                .thenReturn(Optional.of(toCard));

        TransferRequest request = new TransferRequest(
                1L,
                2L,
                new BigDecimal("30.00")
        );

        assertThatThrownBy(() -> cardService.transfer(request, "adil"))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Insufficient funds");

        assertThat(fromCard.getBalance())
                .isEqualByComparingTo("10.00");
        assertThat(toCard.getBalance())
                .isEqualByComparingTo("20.00");
    }

    @Test
    void transferThrowsExceptionWhenCardsAreSame() {
        TransferRequest request = new TransferRequest(
                1L,
                1L,
                new BigDecimal("30.00")
        );

        assertThatThrownBy(() -> cardService.transfer(request, "adil"))
                .isInstanceOf(InvalidTransferException.class)
                .hasMessage("Cannot transfer money to the same card");
    }

    private Card createCard(Long id, BigDecimal balance) {
        User owner = new User();
        owner.setId(1L);
        owner.setUsername("adil");
        owner.setEmail("adil@example.com");
        owner.setPasswordHash("password");
        owner.setRole(Role.USER);

        Card card = new Card();
        card.setId(id);
        card.setEncryptedCardNumber("encrypted");
        card.setNumberHash("hash-" + id);
        card.setLastFour("1234");
        card.setOwner(owner);
        card.setExpiryDate(YearMonth.of(2029, 12));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(balance);

        return card;
    }
}
