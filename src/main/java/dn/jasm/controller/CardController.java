package dn.jasm.controller;

import dn.jasm.configuration.swagger.card.SwaggerCardAnnotation;
import dn.jasm.dto.card.*;
import dn.jasm.dto.card.SetCardResponse;
import dn.jasm.service.CardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Card",description = "Действия с картой для оплаты")
public class CardController {

    private static final String ADD_CARD = "/api/v1/cards/add";
    private static final String DELETE_CARD = "/api/v1/cards/card/delete";
    private static final String GET_LIST_OF_CARDS = "/api/v1/cards/list";
    private static final String GET_COUNT_OF_CARDS_OF_USER = "/api/v1/cards/{userId}/count";
    private static final String GET_CARDS_OF_USER = "/api/v1/cards/{userId}";
    private static final String GET_CARD_BY_ID = "/api/v1/cards/{cardId}";

    private final CardService cardService;

    @GetMapping(GET_CARDS_OF_USER)
    @SwaggerCardAnnotation(operation = "Получение карт пользователя по его уникальному идентификатору ")
    public CardMapResponse getCardsOfUser(@PathVariable Long userId){
        return cardService.getCardsOfUser(userId);
    }

    @GetMapping(GET_CARD_BY_ID)
    @SwaggerCardAnnotation(operation = "Получение карт/ы по её уникальному идентификатору")
    public CardResponse getCardById(@PathVariable Long cardId){
        return cardService.getCardById(cardId);
    }


    @GetMapping(GET_COUNT_OF_CARDS_OF_USER)
    @SwaggerCardAnnotation(operation = "Получение количества привязанных карт к аккаунту")
    public long getCountOfCardOfUser(@PathVariable Long userId){
        return cardService.getCountOfCardOfUser(userId);
    }


    @SwaggerCardAnnotation(operation = "Добавление карты для аккаунта")
    @PostMapping(value = ADD_CARD,produces = MediaType.APPLICATION_JSON_VALUE)
    public void addCard(@RequestBody CardRequest cardRequest,
                        @RequestParam Long userId){
        cardService.addCard(cardRequest,userId);
    }

    @SwaggerCardAnnotation(operation = "Удаление карты из аккаунта")
    @DeleteMapping(DELETE_CARD)
    public void deleteCardFromUser(@RequestParam Long userId,
                                   @RequestParam Long cardId){
        cardService.deleteCardFromUser(userId,cardId);
    }

    @SwaggerCardAnnotation(operation = "Получение списка привязанных карт")
    @GetMapping(GET_LIST_OF_CARDS)
    public SetCardResponse getCardList(@RequestParam(defaultValue = "10",required = false) int pageSize,
                                       @RequestParam(defaultValue = "0",required = false) int pageNumber,
                                       @RequestParam Long userId){
        return cardService.getCardList(pageNumber,pageSize,userId);
    }


}
