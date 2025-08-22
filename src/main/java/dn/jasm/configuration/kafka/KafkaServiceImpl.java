package dn.jasm.configuration.kafka;

import com.ea.async.Async;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import static com.ea.async.Async.*;
import static com.ea.async.Async.await;
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

    @Value("${spring.kafka.template.default-topic}")
    private String topic;

    @Override

    public void sendMessage(String message) {
        if (Objects.equals(message, null)) {
            throw new IllegalArgumentException("Message can't be null");
        }
            Async.init();
            await(kafkaTemplate.send(topic, message)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Message was sent to kafka {}, offset: {}".toUpperCase(),
                                    message,
                                    result.getRecordMetadata().offset());
                        } else {
                            log.error("Cant send message to kafka: {}".toUpperCase(), ex.getMessage());
                        }
                    }));

    }
}
