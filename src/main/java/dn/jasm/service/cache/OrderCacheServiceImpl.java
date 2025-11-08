package dn.jasm.service.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dn.jasm.configuration.redis.CacheNames;
import dn.jasm.dto.order.OrderResponse;
import dn.jasm.mapper.OrderMapper;
import dn.jasm.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCacheServiceImpl implements OrderCacheService{

    private final RedisTemplate<String,Object> redisTemplate;
    private final RedisService redisService;
    private final OrderMapper orderMapper;
    private final ObjectMapper orderObjectMapper;


    @Value("${spring.cache.redis.time-to-live}")
    private Duration TTL;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void putInCache(String id, Object value) {
        if (redisService.checkKeyExist(id)) {
                log.info("[This object: {} already have in cache with key: {}]", value, id);
                return;
            }
            String cacheKey = CacheNames.ORDER_CACHE.getValue() + id;
            redisTemplate.opsForValue().set(cacheKey, value, TTL);
            log.info("[Object: {} put in redis with key: {}]", value, cacheKey);
    }

    @Override
    @SneakyThrows //
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
}
