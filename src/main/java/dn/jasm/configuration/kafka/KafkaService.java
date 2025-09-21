package dn.jasm.configuration.kafka;


public interface KafkaService {
    void sendMessage(String message);

    void sendReactiveMessage(KafkaData kafkaData);
}
