package dn.jasm.configuration.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.security.SecureRandom;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaServiceImpl implements KafkaService {

    private final KafkaTemplate<String,Object> kafkaTemplate;
    private final KafkaSender<String,Object> sender;

    @Value("${spring.kafka.template.default-topic}")
    private String topic;

    @Override
    @Transactional
    public void sendMessage(String message) {
        String key = UUID.randomUUID().toString();
        kafkaTemplate.send(topic,key,message);
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
        sender.send(Mono.just(SenderRecord.create(topic,
                5,
                System.currentTimeMillis(),
                String.valueOf(kafkaData.hashCode()),
                kafkaData,
                null)))
                .subscribe();

    }

}
