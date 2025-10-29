package dn.jasm.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dn.jasm.event.user.UserCreateEvent;
import dn.jasm.service.RabbitService;
import dn.jasm.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitServiceImpl implements RabbitService {

    private final RabbitTemplate rabbitTemplate;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);


    @Value("${rabbitMq.queueName}")
    private String queueName;

    @Value("${rabbitMq.topicExchangeName}")
    private String topicExchangeName;

    @Value("${rabbitMq.routingKey}")
    private String routingKey;


    @Override
    public void completeAsync(UserCreateEvent event){

        CompletableFuture.runAsync(()->
            sendMessage(event),
                        executorService
                )
                .thenRunAsync(()->redisService.writeObjectInRedis(
                        event.getUserId(),
                        event.toString()),
                        executorService
                )
                .whenCompleteAsync((r, e) -> {
            if (e != null) {
                log.error("[Async if failed: {}]", e.getMessage(),e);
            }
            else {
                log.info("[Async completed!]");
            }
        }, executorService);
    }

    public void sendMessage(UserCreateEvent event){
        try {
            var jsonString = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(topicExchangeName,routingKey,jsonString);
        }catch (JsonProcessingException e){
            log.error("Can't serialize cause: {}",e.getOriginalMessage());
        }
    }


    @Override
    @SneakyThrows
    @Async
    public void sendEvent(String queueName,
                          UserCreateEvent userCreateEvent) {
        var jsonString = objectMapper.writeValueAsString(userCreateEvent);
            CompletableFuture<Void> rabbitFuture = CompletableFuture.runAsync(() -> {
                        MessageProperties messageProperties = new MessageProperties();
                        messageProperties.setExpiration("60");
                        Message message = rabbitTemplate.getMessageConverter()
                                .toMessage(
                                userCreateEvent.toString(),
                                messageProperties
                        );
                        try {
                            var value = objectMapper.writeValueAsString(message);
                            rabbitTemplate.convertAndSend(
                                    topicExchangeName,
                                    routingKey,
                                    value
                            );
                        }catch (JsonProcessingException e){
                            log.error("Can't serialize value cause: {}",
                                    e.getMessage());
                        }
                    }, executorService)
                    .thenRunAsync(() -> {
                        redisService.writeObjectInRedis(userCreateEvent.getUserId(),
                                jsonString);
                    }, executorService)
                    .whenCompleteAsync((r, e) -> {
                        if (e != null) {
                            log.error("Async if failed: {}", e.getMessage(),e);
                        }
                        else {
                            log.info("Async completed");
                        }
                    }, executorService);
            log.info("Future is completed: {}",rabbitFuture.toCompletableFuture());
    }
}
