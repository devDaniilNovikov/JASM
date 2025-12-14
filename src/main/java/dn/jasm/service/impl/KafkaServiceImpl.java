package dn.jasm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dn.jasm.service.KafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.listener.adapter.RecordMessagingMessageListenerAdapter;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaServiceImpl implements KafkaService {

    private final KafkaTemplate<String,Object> kafkaTemplate;
    private final RedisTemplate<String,Object> redisTemplate;


    @Primary
    public ExecutorService executorService(){
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Value("${spring.kafka.template.default-topic}")
    private String topic;


    @Override
    @Async
    public void sendMessage(Object message,
                            String id) {
        log.info("KafkaFuture is start");
        var kafkaFuture = CompletableFuture.runAsync(()->{
                kafkaTemplate.send(topic,id,message);
        },executorService());
        log.info("RedisFuture is start");
        var redisFuture = CompletableFuture.runAsync(()->
            redisTemplate.opsForValue()
                    .setIfAbsent(id, message, Duration.ofMinutes(10)));
        CompletableFuture.allOf(redisFuture,kafkaFuture)
                .whenCompleteAsync((r,e)->{
                    if (e!=null){
                        log.error("Error in future: {}",e.getMessage());
                        throw new RuntimeException(e);
                    }},executorService());

    }
}
