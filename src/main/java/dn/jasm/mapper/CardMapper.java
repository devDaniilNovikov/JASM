package dn.jasm.mapper;

import dn.jasm.dto.card.CardResponse;
import dn.jasm.dto.card.SetCardResponse;
import dn.jasm.dto.card.SetCardResponse;
import dn.jasm.entity.CardEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CardMapper {

    public CardResponse toDto(CardEntity cardEntity){
        return CardResponse.builder()
                .id(cardEntity.getId().toString())
                .cardNumber(cardEntity.getCardNumber())
                .cvc(cardEntity.getCvc())
                .date(cardEntity.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy||HH:mm")))
                .dateOfAdding(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy||HH:mm")))
                .build();
    }

    public Set<CardResponse> mapToDtoSet(Set<CardEntity> cardResponseSet){
        return cardResponseSet.stream()
                .map(this::toDto)
                .collect(Collectors.toSet());
    }

    public SetCardResponse mapToSetCardResponse(Set<CardEntity> cards){
        SetCardResponse listCardResponse = new SetCardResponse();
        listCardResponse.setCards(cards.stream().map(this::toDto).collect(Collectors.toSet()));
        return listCardResponse;
    }

    public CardEntity toEntity(CardResponse cardResponse){
        CardEntity card = new CardEntity();
        card.setId(Long.valueOf(cardResponse.getId()));
        card.setCardNumber(cardResponse.getCardNumber());
        card.setCvc(cardResponse.getCvc());
        card.setDate(LocalDateTime.parse(cardResponse.getDate()));
        return card;
    }

}
