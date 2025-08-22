package dn.jasm.configuration.kafka;



@FunctionalInterface
public interface KafkaService {
    void sendMessage(String message);
}
