package dn.jasm.service;

import dn.jasm.dto.card.*;
import dn.jasm.dto.card.ListCardResponse;
import dn.jasm.event.CardCreateEvent;

public interface CardService {

     CardResponse addCard(CardRequest cardRequest, Long userId);

     void deleteCardFromUser(Long userId,Long cardId);

     ListCardResponse getCardList(int pageNumber,
                                  int pageSize,
                                  Long userId);

     long getCountOfCardOfUser(Long userId);

     CardMapResponse getCardsOfUser(Long userId);

     CardResponse getCardById(Long cardId);

     void handleCardCreateEvent(CardCreateEvent cardCreateEvent);







}
