package dn.jasm.configuration.kafka;


import dn.jasm.event.UserCreateEvent;

public interface KafkaService {
    void sendMessage(String message);

    void sendReactiveMessage(KafkaData kafkaData);
}
