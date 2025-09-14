package dn.jasm.configuration.kafka;

import com.ea.async.Async;
import dn.jasm.event.UserCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import static com.ea.async.Async.*;
import static com.ea.async.Async.await;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaServiceImpl implements KafkaService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaSender<String,Object> sender;

    @Value("${spring.kafka.template.default-topic}")
    private String topic;

    @Override
    public void sendMessage(String message) {
        kafkaTemplate.send(topic,message);
    }

    @Override
    @Transactional
    public void sendReactiveMessage(KafkaData kafkaData) {
        String topic = switch (kafkaData.getTopicName()){
            case USER_TOPIC -> "user-topic";
            case TRANSACTION_TOPIC -> "tx_topic";
            case ORDER_TOPIC -> "order_topic";
            case ITEM_TOPIC -> "item_topic";
        };
        sender.send(
                Mono.just(SenderRecord.create(
                topic,
                5,
                System.currentTimeMillis(),
                String.valueOf(kafkaData.hashCode()),
                kafkaData,
                null)))
                .subscribe();

    }

}
