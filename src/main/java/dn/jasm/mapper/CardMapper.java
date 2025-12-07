package dn.jasm.mapper;

import dn.jasm.dto.card.CardRequest;
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
        card.setCardNumber(cardResponse.getCardNumber());
        card.setCvc(cardResponse.getCvc());
        card.setExpMonth(cardResponse.getExpMonth());
        card.setExpYear(cardResponse.getExpYear());
        return card;
    }

    public CardEntity mapToEntity(CardRequest cardRequest){
        CardEntity card = new CardEntity();
        card.setCardNumber(cardRequest.getCardNumber());
        card.setCvc(cardRequest.getCvc());
        card.setExpMonth(cardRequest.getExpMonth());
        card.setExpYear(cardRequest.getExpYear());
        return card;
    }

}
