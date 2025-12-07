package dn.jasm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.jasm.configuration.redis.CacheNames;
import dn.jasm.dto.card.CardMapResponse;
import dn.jasm.dto.card.CardResponse;
import dn.jasm.dto.user.UserResponse;
import dn.jasm.service.RedisService;
import dn.jasm.dto.card.CardRequest;
import dn.jasm.dto.card.ListCardResponse;
import dn.jasm.entity.CardEntity;
import dn.jasm.entity.TransactionEntity;
import dn.jasm.entity.UserEntity;
import dn.jasm.event.CardCreateEvent;
import dn.jasm.exception.CardNotFoundException;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.mapper.CardMapper;
import dn.jasm.repository.CardRepository;
import dn.jasm.repository.TransactionRepository;
import dn.jasm.repository.UserRepository;
import dn.jasm.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisService redisService;
    private final TransactionRepository transactionRepository;
    private final RedisTemplate<String,Object> redisTemplate;
    private static final long CACHE_TTL = 10;
    private final ObjectMapper objectsMapper;

    @Transactional
    @Override
    public CardResponse addCard(CardRequest cardRequest, Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException(
                        MessageFormat.format("[User with id: {0} not found]",userId)
                ));
        var card = cardMapper.mapToEntity(cardRequest);
        cardRepository.save(card);
        card.setUser(user);
        List<CardEntity> cards = user.getCards();
        user.addCard(card);
        userRepository.save(user);
        log.info("[Saved card is: {} of user: {}, cardType: {}]",
                card.getCardNumber(),
                card.getUser().getUsername(),
                card.getCardType());
        publishEvent(card);
        var cacheKey = CacheNames.CARD_CACHE
                .getValue()
                .concat(card.getId()
                        .toString());
        var cardDto = cardMapper.toDto(card);
        redisTemplate.opsForValue()
                .set(cacheKey, cardDto, CACHE_TTL, TimeUnit.MINUTES);
        log.info("[Saving of user with card: {}]",cards);
        return cardDto;

    }


    public void publishEvent(CardEntity card){
        eventPublisher.publishEvent(
                new CardCreateEvent(this,
                        card.getCardNumber(),
                        card.getExpMonth(),
                        card.getExpYear(),
                        card.getCvc(),
                        card.getId().toString())
        );
    }


    @Transactional
    @Override
    public void deleteCardFromUser(Long userId,
                                   Long cardId) {
        var user = userRepository.findById(userId)
                .orElseThrow(RuntimeException::new);
        var card = cardRepository.findById(cardId)
                .orElseThrow(CardNotFoundException::new);
        Set<TransactionEntity> transactionSetOfCard = transactionRepository.findByCardId(cardId);
        if (!transactionSetOfCard.isEmpty()) {
            Set<TransactionEntity> updatedTxs = transactionSetOfCard.stream()
                    .peek(tx->tx.setCard(null))
                    .collect(Collectors.toSet());
            transactionRepository.saveAll(updatedTxs);
            Set<String> updatedTxsIdsAsString = updatedTxs.stream()
                    .map(TransactionEntity::getId)
                    .map(String::valueOf)
                    .collect(Collectors.toSet());
            log.info("[Updated tx's is: {}]",updatedTxsIdsAsString);
        }
        card.setUser(null);
        userRepository.save(user);
        CompletableFuture<Void> completableFutureRedis = CompletableFuture.runAsync(()-> {
                    redisService.deleteCacheByKey(cardId.toString());
                });
        completableFutureRedis.join();
        log.info("[Deleted card: {} of user: {}][",card,user.getUsername());
    }

    @Override
    public ListCardResponse getCardList(int pageNumber,
                                        int pageSize,
                                        Long userId) {
//        PageRequest pageRequest = PageRequest.of(pageNumber,pageSize);
//        List<CardEntity> cards = cardRepository.findAllByUserId(userId);
//        var cacheKeys = cards.stream()
//                .map(CardEntity::getId)
//                .map(String::valueOf)
//                .collect(Collectors.toSet());
//        log.info("[Cards: {}]",cards);
//        redisService.writeObjectsInRedis(cacheKeys, new HashSet<>(cards.getContent()));
//        return cardMapper.mapToSetCardResponse(new HashSet<>(cards.getContent()));

return null;

    }

    @Override
    public long getCountOfCardOfUser(Long userId) {
        return userRepository.findById(userId)
                .stream()
                .map(UserEntity::getCards)
                .mapToInt(Collection::size)
                .peek((count)-> {
                    String cacheKey = String.valueOf(userId);
                    redisService.writeObjectInRedis(cacheKey,count);
                    log.info("[Cached value is: {}, of: {}]",count,cacheKey);
                    log.info("[Count of cards: {}]",count);
                })
                .sum();
    }

    @Override
    public CardMapResponse getCardsOfUser(Long userId) {
        if (userId==null){
            throw new IllegalArgumentException("[UserId can't be null!!!]");
        }
        var cardKeys = cardRepository.findAllByUserId(userId)
                .stream()
                .map(CardEntity::getId)
                .map(String::valueOf)
                .map(key->CacheNames.CARD_CACHE
                        .getValue()
                        .concat(key))
                .toList();
        var cardCacheKeys = redisTemplate.opsForValue().multiGet(cardKeys);
        if (cardCacheKeys!=null && cardCacheKeys.stream()
                .allMatch(Objects::nonNull)){
            cacheLogging();
            List<CardResponse> cards = cardCacheKeys.stream()
                    .map(key->objectsMapper.convertValue(key, CardResponse.class))
                    .toList();
            var userName = cards.stream()
                    .map(cardMapper::toEntity)
                    .map(CardEntity::getUser)
                    .map(UserEntity::getUsername)
                    .findAny()
                    .orElseThrow(UserNotFoundException::new);
            CardMapResponse cardMapResponse = new CardMapResponse();
            cardMapResponse.setCardsOfUser(Map.of(userName,cards));
            return cardMapResponse;
        }
        dataBaseLogging();
        var user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        CardMapResponse cardMapResponse = new CardMapResponse();
        var cards = user.getCards()
                .stream()
                .map(cardMapper::toDto)
                .toList();
        cardMapResponse.setCardsOfUser(Map.of(user.getUsername(),cards));
        return cardMapResponse;
    }

    private static void cacheLogging() {
        log.info("Value will get from cache");
    }

    private static void dataBaseLogging() {
        log.info("Value will get from db");
    }

    @Override
    public CardResponse getCardById(Long cardId) {
        var cacheKey = CacheNames.CARD_CACHE
                .getValue()
                .concat(cardId.toString());
        var cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (cacheValue!=null){
            cacheLogging();
            return objectsMapper.convertValue(cacheValue, CardResponse.class);
        }
        dataBaseLogging();
        return cardRepository.findById(cardId)
                .stream()
                .map(cardMapper::toDto)
                .peek(card->redisTemplate.opsForValue().set(
                        cacheKey,card,CACHE_TTL,TimeUnit.MINUTES
                ))
                .findAny()
                .orElseThrow(CardNotFoundException::new);
    }

    @Override
    @EventListener
    public void handleCardCreateEvent(CardCreateEvent cardCreateEvent) {
        redisService.writeObjectInRedis(cardCreateEvent.getId(),
                cardCreateEvent.toString());

        log.info("[Created card is: {}]",cardCreateEvent.toString());
    }


}
