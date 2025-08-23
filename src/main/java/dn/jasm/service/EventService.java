package dn.jasm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dn.jasm.configuration.aop.Loggable;
import dn.jasm.entity.enums.TransactionStatus;
import dn.jasm.event.CommentEvent;
import dn.jasm.configuration.kafka.KafkaService;
import dn.jasm.event.MailMessageEvent;
import dn.jasm.configuration.redis.RedisService;
import dn.jasm.event.TransactionEvent;
import dn.jasm.event.UserCreateEvent;
import dn.jasm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final KafkaService kafkaService;
    private final RedisService redisService;
    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @EventListener
    public void handleEvent(MailMessageEvent event){
        completedFuture(event);
        if (event!=null){
            redisService.writeObjectInRedis(event.getTo(),event.getContent());
            log.info("Writed event to cache: {}",event.toString());
        }
    }

//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handleTransactionEvent(TransactionEvent transactionEvent){
//        try {
//            var tx = transactionService.getTransactionById(transactionEvent.getTxId());
//            if (tx.getCompletedAt()) {
//                tx.setTransactionStatus(TransactionStatus.COMPLETED);
//            }
//            tx.setTransactionStatus(TransactionStatus.CANCELLED);
//        }catch (TransactionException e){
//            log.error("Error complete trancation: {}",e.getMessage());
//        }
//    }

    @EventListener
    public void setLog(TransactionEvent txEvent){
        log.info("Cancelled transaction is: {}",txEvent.getTxId());
    }


    @EventListener
    @Loggable
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
            kafkaService.sendMessage(message);
        } catch (JsonProcessingException e) {
            log.error("Can't serialize userCreate message: {}", e.getMessage());
        }
        
    }

    @EventListener
    @Loggable
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