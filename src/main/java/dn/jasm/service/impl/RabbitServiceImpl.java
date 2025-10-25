package dn.jasm.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import dn.jasm.event.user.UserCreateEvent;
import dn.jasm.service.RabbitService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitServiceImpl implements RabbitService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rabbitMq.queueName}")
    private String queueName;

    @Value("${rabbitMq.topicExchangeName}")
    private String topicExchangeName;

    @Value("${rabbitMq.routingKey}")
    private String routingKey;


    @Override
    @SneakyThrows
    public void sendEvent(UserCreateEvent userCreateEvent) {
        var jsonString = objectMapper.writeValueAsString(userCreateEvent);
        rabbitTemplate.convertAndSend(
                topicExchangeName,
                routingKey,
                jsonString
        );

    }
}
