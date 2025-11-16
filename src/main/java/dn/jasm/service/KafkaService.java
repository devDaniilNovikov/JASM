package dn.jasm.service;

public interface KafkaService {

    void sendMessage(Object message,String id);
}
