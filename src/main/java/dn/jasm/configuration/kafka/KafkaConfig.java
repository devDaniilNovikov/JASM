package dn.jasm.configuration.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.converter.BatchMessagingMessageConverter;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServerValue;

    @Value("${spring.kafka.template.default-topic}")
    private String topicName;

    @Bean
    public DefaultKafkaProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> producerProperties = kafkaProperties.buildProducerProperties(null);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServerValue);
        producerProperties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG,"tx-");
        DefaultKafkaProducerFactory<String,Object> kafkaProducerFactory = new DefaultKafkaProducerFactory<>(producerProperties);
        kafkaProducerFactory.setTransactionIdPrefix("tx-");
        return kafkaProducerFactory;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(DefaultKafkaProducerFactory<String, Object> producerFactory){
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties(null);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "java.lang");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public KafkaListenerContainerFactory<?> listenerContainerFactory(ConsumerFactory<String, Object> stringConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stringConsumerFactory);
        factory.setBatchListener(true);
        return factory;
    }

    @Bean
    public NewTopic newTopic(){
        return TopicBuilder.name(topicName)
                .partitions(5)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG,
                 String.valueOf(Duration.ofDays(14)
                .toMillis()))
                .build();
    }

    @Bean
    public SenderOptions<String,Object> senderOptions(){
        Map<String,Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServerValue);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,JsonSerializer.class);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG,10);
        return SenderOptions.create(props);
    }

    @Bean
    public KafkaSender<String,Object> kafkaSender(){
        return KafkaSender.create(senderOptions());
    }



    @Bean
    public RecordMessageConverter recordMessageConverter(){
        return new JsonMessageConverter();
    }

    @Bean
    public BatchMessagingMessageConverter batchMessagingMessageConverter(){
        return new BatchMessagingMessageConverter(recordMessageConverter());
    }
//
//    @Bean(name = "transactionManager")
//    public KafkaTransactionManager<String,Object> kafkaTransactionManager(ProducerFactory<String,Object> producerFactory){
//        KafkaTransactionManager<String,Object> kafkaTransactionManager = new KafkaTransactionManager<>(producerFactory);
//        kafkaTransactionManager.setTransactionSynchronization(AbstractPlatformTransactionManager.SYNCHRONIZATION_ON_ACTUAL_TRANSACTION);
//        return kafkaTransactionManager;
//
//    }
}

