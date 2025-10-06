package dn.jasm.service;

import dn.jasm.dto.card.*;
import dn.jasm.dto.card.SetCardResponse;
import dn.jasm.entity.CardEntity;
import dn.jasm.event.CardCreateEvent;

import java.util.Set;

public interface CardService {

     CardResponse addCard(CardRequest cardRequest, Long userId);

     void deleteCardFromUser(Long userId,Long cardId);

     SetCardResponse getCardList(int pageNumber,
                                 int pageSize,
                                 Long userId);

     long getCountOfCardOfUser(Long userId);

     CardMapResponse getCardsOfUser(Long userId);

     CardResponse getCardById(Long cardId);

     void handleCardCreateEvent(CardCreateEvent cardCreateEvent);







}
