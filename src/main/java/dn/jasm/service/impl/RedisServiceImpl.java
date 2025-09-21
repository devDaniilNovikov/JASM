package dn.jasm.service.impl;


import dn.jasm.configuration.aop.Loggable;
import dn.jasm.exception.RedisKeyException;
import dn.jasm.service.RedisService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisPool;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisServiceImpl implements RedisService {

    @Value("${spring.cache.redis.time-to-live}")
    private Long TTL;

    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    public void writeObjectInRedis(String key, Object object) {
        try {
            if (!checkKeyExist(key)) {
                redisTemplate.opsForValue().set(key, object);
                redisTemplate.expire(key, TTL, TimeUnit.MINUTES);
                log.info("Saving value: {} to cache with key: {}", object, key);
            }
        }catch (RedisKeyException e){
            log.error("Key already have in redis: {}",e.getMessage());
        }
    }




    @Override
    @Transactional
    public void writeObjectsInRedis(Set<String> keys, Set<Object> objects) {
        try {
            Set<String> existingKeys = keys.stream()
                    .filter(this::checkKeyExist)
                    .collect(Collectors.toSet());
            if (!existingKeys.isEmpty()) {
                throw new RedisKeyException(MessageFormat.format("Expiry keys : {0}",existingKeys));
            }
            IntStream.range(0, keys.size()).forEach(key -> {
                redisTemplate.opsForSet().add(String.valueOf(keys),objects);
                redisTemplate.expire(keys.toString(),TTL,TimeUnit.MINUTES);
                log.info("Redis keys is: {}".toUpperCase(), keys);
            });
        }catch (RedisKeyException e){
            log.error("That keys already have in cache: {}", e.getMessage());
        }
    }


    @Override
    public boolean checkKeyExist(String key) {
        if (Objects.equals(key,null)){
            throw new IllegalArgumentException("Key can't be null");
        }
        return redisTemplate.hasKey(key);
    }

    @Override
    public boolean checkKeysExist(Set<String> keys){
        if (keys.isEmpty()){
            throw new IllegalArgumentException("Keys can't be null!".toUpperCase());
        }
        return keys.stream()
                .map(this::checkKeyExist)
                .reduce(false,(trueKey,falseKey)-> trueKey || falseKey);

    }

    @Override
    @Transactional
    public void deleteCacheByKey(String key) {
        if (Objects.equals(key, null)) {
            log.error("Key is null!");
            throw new IllegalArgumentException("Key can't b null");
        }
        if (redisTemplate.opsForValue().get(key) != null){
            redisTemplate.delete(key);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Object getByKey(String key) {
       return redisTemplate.opsForValue().get(key);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getKeySet(String keyPattern) {
        if (keyPattern.equals("*")) {
            return redisTemplate.keys("*");
        }
        throw new RedisKeyException("Can't find key");
    }

    @Transactional
    @Override
    public void deleteCachesByKeys(Set<String> keys) {
        if (redisTemplate.opsForValue().get(keys)!=null){
            redisTemplate.delete(keys);
        }
    }


}
