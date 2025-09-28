package dn.jasm.configuration.redis;


import dn.jasm.entity.enums.OrderStatus;
import dn.jasm.event.MailMessageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.mail.dsl.Mail;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedisSchema {

    private final RedisKeyGenerator redisKeyGenerator;

    @Value("${spring.cache.cache-names}")
    private  List<String> cacheNamesSet;

    public static String userKeys(){
        return RedisKeyGenerator.getKey("users");
    }

    public static String paymentKey(long userId, OrderStatus orderStatus){
        return RedisKeyGenerator.getKey("users:" + userId + ":" + orderStatus.name().toLowerCase());
    }
    public static String mailKey(){
        return RedisKeyGenerator.getKey("mail");
    }

}
