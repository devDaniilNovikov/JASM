package dn.jasm.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dn.jasm.configuration.redis.RedisSchema;
import dn.jasm.event.MailMessageEvent;
import dn.jasm.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Properties;

import static java.util.concurrent.CompletableFuture.completedFuture;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class JavaMailConfig {

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.password}")
    private String password;

    private final RedisService redisService;
    private final JedisPool jedisPool;

    @Bean
    public JavaMailSender javaMailSender(){
        JavaMailSenderImpl javaMailSenderImpl = new JavaMailSenderImpl();
        javaMailSenderImpl.setPort(port);
        javaMailSenderImpl.setHost(host);
        javaMailSenderImpl.setUsername(username);
        javaMailSenderImpl.setPassword(password);
        Properties props = javaMailSenderImpl.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        return javaMailSenderImpl;
    }

    @EventListener
    public void handleEvent(MailMessageEvent event){
        completedFuture(event);
        if (event!=null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                try (Jedis jedis = jedisPool.getResource()){
                    jedis.sismember(RedisSchema.mailKey(),event.getTo());
                    log.error("Jedis keys: {}",jedis.get(event.getTo()));
                }
                var cacheString = objectMapper.writeValueAsString(event);
                redisService.writeObjectInRedis(event.getTo(), cacheString.getClass());
                log.info("Written event to cache: {}", event);
            } catch (JsonProcessingException e) {
                log.error("Can't write value in cache: {}",e.getMessage());

            }
        }
    }




}
