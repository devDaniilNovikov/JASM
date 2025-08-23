package dn.jasm.configuration.redis;


import dn.jasm.configuration.aop.Loggable;
import dn.jasm.configuration.aop.TimeResulting;
import dn.jasm.exception.RedisKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.ref.SoftReference;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final int TTL = 10;



    @Override
    @Loggable
    @TimeResulting
    public void writeObjectInRedis(String key, Object object) {
        if (checkKeyExist(key)){
            throw new RedisKeyException("Cache value by this keys already have in redis");
        }
        redisTemplate.opsForValue().set(key,object);
    }




    @Loggable
    @Override
    @Transactional
    public void writeObjectsInRedis(List<String> keys, List<Object> objects) {
        try {
            List<String> existingKeys = keys.stream()
                    .filter(this::checkKeyExist)
                    .toList();
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
    public boolean checkKeysExist(List<String> keys){
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
        if (redisTemplate.opsForValue().get(key) == null){
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

    @Override
    public void deleteCachesByKeys(List<String> keys) {
        redisTemplate.delete(keys);
    }


}
