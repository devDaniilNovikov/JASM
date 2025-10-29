package dn.jasm.configuration.rabbit;

import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.retry.backoff.BackOffPolicyBuilder;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
public class RabbitConfig {

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${rabbitMq.queueName}")
    private String queueName;

    @Value("${rabbitMq.topicExchangeName}")
    private String topicExchangeName;

    @Value("${rabbitMq.routingKey}")
    private String routingKey;

    @Value("${rabbitMq.directExchange}")
    private String directExchange;

    @Value("${rabbitMq.ttl}")
    private int ttl;

    @Value("${rabbitMq.headerName}")
    private String headerName;

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(3,threadFactory());
    }

    @Bean("taskExecutor")
    public ThreadFactory threadFactory(){
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setQueueCapacity(25);
        taskExecutor.setMaxPoolSize(10);
        taskExecutor.setThreadNamePrefix("JasmAsyncJobs-");
        taskExecutor.setVirtualThreads(true);
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setRoutingKey(routingKey);
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setExchange(topicExchangeName);
        rabbitTemplate.setRetryTemplate(retryTemplate());
        rabbitTemplate.setTaskExecutor(executorService());
        rabbitTemplate.afterPropertiesSet();
        return rabbitTemplate;
    }

    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange(directExchange,true,false);
    }

    @Bean
    public RabbitTemplateConfigurer rabbitTemplateConfigurer(ConnectionFactory connectionFactory){
        RabbitTemplateConfigurer rabbitTemplateConfigurer = new RabbitTemplateConfigurer(rabbitProperties());
        rabbitTemplateConfigurer.setMessageConverter(messageConverter());
        rabbitTemplateConfigurer.configure(rabbitTemplate(connectionFactory),connectionFactory);
        return rabbitTemplateConfigurer;
    }

    @Bean
    @Primary
    public RabbitProperties rabbitProperties(){
        RabbitProperties rabbitProperties = new RabbitProperties();
        rabbitProperties.setHost(host);
        rabbitProperties.setPort(port);
        rabbitProperties.setUsername(username);
        rabbitProperties.setPassword(password);
        return rabbitProperties;
    }

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }



    @Bean
    public Queue queue(){
        return QueueBuilder.durable()
                .stream()
                .singleActiveConsumer()
                .exclusive()
                .withArgument("queue-name",queueName)
                .ttl(ttl)
                .autoDelete()
                .overflow(QueueBuilder.Overflow.rejectPublish)
                .deliveryLimit(5)
                .expires(10)
                .build();
    }

    @Bean
    @Primary
    public HeadersExchange headersExchange(){
        HeadersExchange headersExchange = new HeadersExchange(headerName);
        headersExchange.setDelayed(false);
        headersExchange.setInternal(true);
        headersExchange.setShouldDeclare(true);
        headersExchange.addArgument("topic-name",topicExchangeName);
        return headersExchange;
    }

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(topicExchangeName);
    }

    @Bean
    public Binding binding(){
        return BindingBuilder.bind(queue())
                .to(topicExchange())
                .with(routingKey);
    }

    @Bean
    public RetryTemplate retryTemplate(){
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy(3);
        BackOffPolicyBuilder backOffPolicyBuilder = BackOffPolicyBuilder.newBuilder()
                .delay(10)
                .multiplier(2.0)
                .random(true);
        var backOffPolicy = backOffPolicyBuilder.build();
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(simpleRetryPolicy);
        return retryTemplate;

    }





















}
