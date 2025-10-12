package dn.jasm.service.impl;

import dn.jasm.dto.card.CardMapResponse;
import dn.jasm.dto.card.CardResponse;
import dn.jasm.service.RedisService;
import dn.jasm.dto.card.CardRequest;
import dn.jasm.dto.card.SetCardResponse;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Transactional
    @Override
    public CardResponse addCard(CardRequest cardRequest, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException(""));
        CardEntity card = new CardEntity();
        card.setId(card.getId());
        card.setCardNumber(cardRequest.getCardNumber());
        card.setCvc(cardRequest.getCvc());
        card.setDate(LocalDateTime.now());
        card.setCardType(cardRequest.getCardType());
        var userCards = user.getCards();
        userCards.add(card);
        cardRepository.save(card);
        card.setUser(user);
        log.info("[Saved card is: {} of user: {}, cardType: {}]",
                card.getCardNumber(),
                card.getUser().getUsername(),
                card.getCardType());
        userRepository.save(user);
        publishEvent(card);
        var cacheValue = cardMapper.toDto(card);
        redisService.writeObjectInRedis(cacheValue.getId(),cacheValue.toString());
        log.info("[Saving of user with card: {}]",user.getCards());
        return cardMapper.toDto(card);

    }

    @Async
    protected void publishEvent(CardEntity card){
        eventPublisher.publishEvent(
                new CardCreateEvent(this,
                        card.getCardNumber(),
                        card.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy | HH:mm")),
                        card.getCvc(),
                        card.getId().toString())
        );
    }


    @Transactional
    @Override
    public void deleteCardFromUser(Long userId,Long cardId) {
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
    public SetCardResponse getCardList(int pageNumber,
                                       int pageSize,
                                       Long userId) {
        PageRequest pageRequest = PageRequest.of(pageNumber,pageSize);
        Page<CardEntity> cards = cardRepository.findAllByUserId(userId,pageRequest);
        var cacheKeys = cards.stream()
                .map(CardEntity::getId)
                .map(String::valueOf)
                .collect(Collectors.toSet());
        log.info("[Cards: {}]",cards);
        redisService.writeObjectsInRedis(cacheKeys, new HashSet<>(cards.getContent()));
        return cardMapper.mapToSetCardResponse(new HashSet<>(cards.getContent()));



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
        CardMapResponse cardMapResponse = new CardMapResponse();
        Map<String, Set<CardResponse>> map = new ConcurrentHashMap<>();
        var cards = userRepository.findById(userId)
                .stream()
                .map(UserEntity::getCards)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(cardMapper::toDto)
                .sorted(Comparator.comparing(CardResponse::getDateOfAdding))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        var username = userRepository.findById(userId)
                .stream()
                .map(UserEntity::getUsername)
                .findAny()
                .orElseThrow(UserNotFoundException::new);
        map.put(username, cards);
        cardMapResponse.setCardsOfUser(map);
        var cacheValue = map.get(username);
        redisService.writeObjectInRedis(username,cacheValue);
        return cardMapResponse;
    }

    @Override
    public CardResponse getCardById(Long cardId) {
        return cardRepository.findById(cardId)
                .stream()
                .map(cardMapper::toDto)
                .peek(card->redisService.writeObjectInRedis(cardId.toString(),card.toString()))
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
