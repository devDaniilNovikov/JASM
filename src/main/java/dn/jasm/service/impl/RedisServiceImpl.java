package dn.jasm.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import dn.jasm.configuration.redis.CacheNames;
import dn.jasm.exception.RedisKeyException;
import dn.jasm.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String,Object> redisTemplate;
    private static final Duration TTL = Duration.ofMinutes(10);


    @Override
    public void writeObjectInRedis(String key, Object object) {

    }

    @Override
    public void writeObjectsInRedis(Set<String> keys, Set<Object> objects) {

    }

    @Override
    public boolean checkKeyExist(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public boolean checkKeysExist(Set<String> keys) {
        return false;
    }

    @Override
    public void deleteCacheByKey(String key) {

    }

    @Override
    public Object getFromCache(String key, CacheNames cacheNames) {
        switch (cacheNames){
            case USER_CACHE, CARD_CACHE -> redisTemplate.opsForValue().get(key);
        }
        return redisTemplate.opsForValue().get(key);
    }



    @Override
    public Set<String> getKeySet(String keyPattern) {
        return Set.of();
    }

    @Override
    public void deleteCachesByKeys(Set<String> keys) {

    }

    @Override
    public <T> void writeEventInRedis(T t) {

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW,
            timeout = 1,
            isolation = Isolation.REPEATABLE_READ)
    public void putToCache(String key,
                           Object value,
                           CacheNames cacheNames) {
        switch (cacheNames){
            case USER_CACHE -> {
                CacheNames userCache = CacheNames.USER_CACHE;
                var redisKey = userCache.getValue()+key;
                writeInRedis(redisKey,value);
                log(value,redisKey);
            }
            case PAYMENT_CACHE -> {
                CacheNames paymentCache = CacheNames.PAYMENT_CACHE;
                var redisKey = paymentCache.getValue()+key;
                writeInRedis(redisKey,value);
                log(value,redisKey);
            }
            case ORDER_CACHE -> {
                CacheNames orderCache = CacheNames.ORDER_CACHE;
                var redisKey = orderCache.getValue()+key;
                writeInRedis(redisKey,value);
                log(value,redisKey);
            }
            case TRANSACTION_CACHE -> {
                CacheNames txCache = CacheNames.TRANSACTION_CACHE;
                var redisKey = txCache.getValue()+key;
                writeInRedis(redisKey,value);
                log(value,redisKey);
            }
            case CARD_CACHE -> {
                CacheNames cardCache = CacheNames.CARD_CACHE;
                var redisKey = cardCache.getValue()+key;
                writeInRedis(redisKey,value);
                log(value,redisKey);
            }
            case SHOP_CACHE -> {
                CacheNames shopCache = CacheNames.SHOP_CACHE;
                var redisKey = shopCache.getValue()+key;
                writeInRedis(redisKey,value);
                log(value,redisKey);
            }
            case COMMENT_CACHE -> {
                CacheNames commentCache = CacheNames.COMMENT_CACHE;
                var redisKey = commentCache.getValue()+key;
                writeInRedis(redisKey,value);
                log(value,redisKey);
            }
            case ITEM_CACHE -> {
                CacheNames itemCache = CacheNames.ITEM_CACHE;
                var redisKey = itemCache.getValue()+key;
                writeInRedis(redisKey,value);
                log(value,redisKey);
            }
            default -> {
                throw new RuntimeException("");
            }
        }
    }

    private static void log(Object value,
                            String key){
        log.info("[Object: {} was put in redis with key: {}]",value,key);
    }

    private void writeInRedis(String redisKey,
                              Object value){
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))){
            throw new RedisKeyException(
                    MessageFormat.format(
                            "[Value: {0} with key: {1} already have been in cache]",value,redisKey)
            );
        }
        try {
            redisTemplate.opsForValue().set(redisKey, value);
            redisTemplate.expire(redisKey, TTL);
        }catch (RedisKeyException e){
            log.error("Can't write in cache: {}",value);
        }
    }

    public boolean existsAt(String key){
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }






}
