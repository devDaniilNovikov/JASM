package dn.jasm.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dn.jasm.configuration.aop.Loggable;
import dn.jasm.configuration.redis.RedisSchema;
import dn.jasm.dto.transaction.TransactionDto;
import dn.jasm.entity.UserEntity;
import dn.jasm.entity.enums.TransactionStatus;
import dn.jasm.event.*;
import dn.jasm.configuration.kafka.KafkaService;
import dn.jasm.repository.CardRepository;
import dn.jasm.service.RedisService;
import dn.jasm.exception.TransactionNotFoundException;
import dn.jasm.mapper.TransactionMapper;
import dn.jasm.repository.TransactionRepository;
import dn.jasm.repository.UserRepository;
import dn.jasm.service.EventService;
import dn.jasm.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;
    private final CardRepository cardRepository;
    private final TransactionService transactionService;
    private final KafkaService kafkaService;
    private final RedisService redisService;
    private final RedisTemplate<String,Object> redisTemplate;
    private final JedisPool jedisPool;

    @EventListener
    @Override
    public void handleEvent(MailMessageEvent event){
        completedFuture(event);
        if (event!=null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                try (Jedis jedis = jedisPool.getResource()){
                    jedis.sismember(RedisSchema.userKeys(),event.getTo());
                    log.error("Jedis keys: {}",jedis.get(event.getTo()));
                }
                var cacheString = objectMapper.writeValueAsString(event);
                redisService.writeObjectInRedis(event.getTo(), cacheString);
                log.info("Writed event to cache: {}", event.toString());
            } catch (JsonProcessingException e) {
                log.error("Can't write value in cache: {}",e.getMessage());

            }
        }
    }

    @Override
    public void handleEvent(UserUpdateEvent userUpdateEvent) {
        log.info("Updating user: {}",userUpdateEvent.getIsUpdate());
    }

    @EventListener
    @Override
    public void handleEvent(CardCreateEvent cardCreateEvent) {
        redisTemplate.opsForValue().set(cardCreateEvent.getId(),cardCreateEvent.toString());
        redisTemplate.expire(cardCreateEvent.getId(),5,TimeUnit.MINUTES);

        log.info("Created card is: {}",cardCreateEvent.toString());
    }


    @EventListener
    @Override
    public void handleEvent(TransactionEvent transactionEvent){
        BigDecimal userTotalBalance = userRepository.findById(transactionEvent.getUserId())
                .stream()
                .map(user->{
                    BigDecimal totalBalance = user.getBalance().subtract(transactionEvent.getTotalAmount());
                    user.setBalance(totalBalance);
                    return userRepository.save(user);
                })
                .map(UserEntity::getBalance)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        if (transactionEvent.getCompletedAt()){
            try {
                var txId = transactionEvent.getTxId();
                var transaction = transactionRepository.findById(txId)
                        .orElseThrow(() -> new TransactionNotFoundException(
                                MessageFormat.format("Transaction with id: {0} not found", transactionEvent.getTxId())
                        ));
                var cardForTx = cardRepository.findById(transactionEvent.getCardId())
                                .orElseThrow(RuntimeException::new);
                transaction.setTransactionStatus(TransactionStatus.COMPLETED);
                transaction.setCard(cardForTx);
                transactionRepository.save(transaction);
                var txStatus = transaction.getTransactionStatus();
                var cacheTx = transactionMapper.mapToDto(transaction);
                putToCache(cacheTx);
                log.info("Successfully processing of transaction with id: {}, status: {} ", txId, txStatus);
            } catch (Exception e) {
                log.error("Exception in processing of transaction: {}",e.getMessage());
                throw new RuntimeException(e);
            }
        }
        log.info("Total balance of user is: {}",userTotalBalance);
    }

    private void putToCache(TransactionDto transactionDto) {
        putInRedis(transactionDto, redisService);
    }

    public static void putInRedis(TransactionDto transactionDto, RedisService redisService) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try {
            var txCacheKey = objectMapper.writeValueAsString(transactionDto.getTxId());
            var txCacheValue = objectMapper.writeValueAsString(transactionDto);
            redisService.writeObjectInRedis(txCacheKey, txCacheValue);
            log.info("Cache string: {}",txCacheValue);
        } catch (JsonProcessingException e) {
            log.error("Can't serialize value: {}", e.getMessage());
        }
    }


    public void handleTxCancelEvent(TransactionEvent txEvent){
        transactionService.cancelTransaction(txEvent.getTxId());
        log.info("Handle event for delete tx:{}",txEvent.getTxId());
    }


    @EventListener
    @Loggable
    @Override
    public void handleEvent(UserCreateEvent userCreateEvent) {
        if (userCreateEvent == null){
            log.error("Event is null");
            throw new RuntimeException("Event can't be null");
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            String message = objectMapper.writeValueAsString(userCreateEvent);
            redisService.writeObjectInRedis(userCreateEvent.getUserId(),message);
        } catch (JsonProcessingException e) {
            log.error("Can't serialize userCreate message: {}", e.getMessage());
        }
        
    }

    @EventListener
    @Loggable
    @Override
    public void handleEvent(CommentEvent commentEvent) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            String message = objectMapper.writeValueAsString(commentEvent);

            CompletableFuture<Void> kafkaFuture = CompletableFuture.runAsync(() ->
                    kafkaService.sendMessage(message));
            CompletableFuture<Void> redisFuture = CompletableFuture.runAsync(() ->
                    redisService.writeObjectInRedis(commentEvent.getComment(), message));
            CompletableFuture.allOf(kafkaFuture, redisFuture)
                    .exceptionally(throwable -> {
                        log.error("Error processing comment event: {}", throwable.getMessage());
                        return null;
                    });
        } catch (JsonProcessingException e) {
            log.error("Can't serialize comment message: {}", e.getMessage());
        }
    }


}