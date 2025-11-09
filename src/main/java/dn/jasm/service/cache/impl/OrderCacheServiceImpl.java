package dn.jasm.service.cache.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.param.treasury.TransactionListParams;
import dn.jasm.configuration.redis.CacheNames;
import dn.jasm.dto.order.OrderResponse;
import dn.jasm.exception.OrderNotFoundException;
import dn.jasm.mapper.OrderMapper;
import dn.jasm.service.RedisService;
import dn.jasm.service.cache.OrderCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCacheServiceImpl implements OrderCacheService   {

    private final RedisTemplate<String,Object> redisTemplate;
    private final ObjectMapper orderObjectMapper;




    @Value("${spring.cache.redis.time-to-live}")
    private Duration TTL;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void putInCache(String id, Object value) {
        String key = CacheNames.ORDER_CACHE.getValue()+id;
        if (redisTemplate.hasKey(key)) {
                log.info("[This object: {} already have in cache with key: {}]", value, id);
                return;
            }
            redisTemplate.opsForValue().set(key, value, TTL);
            log.info("[Object: {} put in redis with key: {}]", value, key);
    }

    @Override
    public OrderResponse getOrderFromCache(String id) {
        String cacheKey = CacheNames.ORDER_CACHE.getValue()+id;
        if (redisTemplate.hasKey(cacheKey)){
            var value = redisTemplate.opsForValue().get(id);
            log.info("[Object: {} already have in redis]",value);
            return orderObjectMapper.convertValue(value,OrderResponse.class);
        }
        Object cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (cacheValue!=null){
            return orderObjectMapper.convertValue(cacheValue, OrderResponse.class);
        }
        return null;
    }

    @Override
    public void deleteFromCache(String id) {
        String key = CacheNames.ORDER_CACHE.getValue()+id;
        var elementForDelete = redisTemplate.opsForValue().get(key);
        if (elementForDelete!=null){
            log.info("[Deleted element: {}]",elementForDelete);
            redisTemplate.delete(id);
        }
    }

    @Transactional
    public void deleteFromCache(Set<String> keys){
        if (keys==null){
            throw new IllegalArgumentException("[Keys can't be null]");
        }
        Set<String> ids = keys.stream()
                .map(key->CacheNames.ORDER_CACHE.getValue()+key)
                .collect(Collectors.toSet());
        Optional.ofNullable(redisTemplate.opsForValue()
                        .multiGet(ids))
                        .ifPresentOrElse(key-> {
                            redisTemplate.delete(ids);
                        },()->{
                            throw new OrderNotFoundException(
                                    MessageFormat.format(
                                            "Order cache with ids: {0} not found",ids));
                        });
    }

}
