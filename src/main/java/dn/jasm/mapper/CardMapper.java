package dn.jasm.mapper;

import dn.jasm.dto.card.CardResponse;
import dn.jasm.dto.card.ListCardResponse;
import dn.jasm.entity.CardEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CardMapper {

    public CardResponse toDto(CardEntity cardEntity){
        return CardResponse.builder()
                .id(cardEntity.getId().toString())
                .cardNumber(cardEntity.getCardNumber())
                .cvc(cardEntity.getCvc())
                .expMonth(cardEntity.getExpMonth())
                .expYear(cardEntity.getExpYear())
                .build();
    }

    public Set<CardResponse> mapToDtoSet(Set<CardEntity> cardResponseSet){
        return cardResponseSet.stream()
                .map(this::toDto)
                .collect(Collectors.toSet());
    }

    public ListCardResponse mapToSetCardResponse(Set<CardEntity> cards){
        ListCardResponse listCardResponse = new ListCardResponse();
        listCardResponse.setCards(cards.stream()
                .map(this::toDto)
                .collect(Collectors.toList()));
        return listCardResponse;
    }

    public CardEntity toEntity(CardResponse cardResponse){
        CardEntity card = new CardEntity();
        card.setId(Long.valueOf(cardResponse.getId()));
        card.setCardNumber(cardResponse.getCardNumber());
        card.setCvc(cardResponse.getCvc());
        card.setExpMonth(cardResponse.getExpMonth());
        card.setExpYear(cardResponse.getExpYear());
        return card;
    }

}
