package dn.jasm.service.cache.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import dn.jasm.configuration.redis.CacheNames;
import dn.jasm.dto.user.UserResponse;
import dn.jasm.dto.user.UserResponseList;
import dn.jasm.service.cache.UserCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCacheServiceImpl implements UserCacheService  {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectsMapper;

    @Value("${spring.cache.redis.time-to-live}")
    private Duration TTL;



    @Override
    public UserResponse getValueFromCache(String id) {
        String cacheKey = CacheNames.USER_CACHE
                .getValue()
                .concat(id);
        var value = redisTemplate.opsForValue().get(cacheKey);
        if (value!=null){
            log.info("Element will put in cache: {}",value);
            return objectsMapper.convertValue(value, UserResponse.class);
        }
        return null;

    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseList getValuesFromCache(Set<String> strings) {
        List<String> keys = strings.stream()
                .filter(Objects::nonNull)
                .toList();
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        if (values != null && values.stream()
                .anyMatch(Objects::nonNull)) {
            List<UserResponse> users = values.stream()
                    .map(user -> objectsMapper.convertValue(user, UserResponse.class))
                    .toList();
            if (!users.isEmpty()) {
                UserResponseList response = new UserResponseList();
                response.setUsers(users);
                return response;
            }
        }
        return null;
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            rollbackFor = Exception.class,
            isolation = Isolation.REPEATABLE_READ
    )
    public void putToCache(String key, Object value) {
        if (value==null){
            return;
        }
        var redisKey = CacheNames.USER_CACHE.getValue()+key;
        redisTemplate.opsForValue().set(
                redisKey,
                value,
                TTL
        );
    }

    @Override
    @Transactional
    public void putValuesToCache(List<String> keys,
                                 List<Object> values) {
        Map<String,Object> map = new HashMap<>();
        for (int i = 0; i<keys.size();i++){
            String key = CacheNames.USER_CACHE.getValue()+keys;
            map.put(key,values);
            log.info("Map with cacheValues is: {}",map);
        }
        redisTemplate.opsForValue().multiSet(map);
    }

    @Override
    public void deleteFromCache(String id) {
        String key = CacheNames.USER_CACHE.getValue()+id;
        var elementForDelete = redisTemplate.opsForValue().get(key);
        if (elementForDelete!=null){
            log.info("[Deleted element: {}]",elementForDelete);
            redisTemplate.delete(id);
        }
    }
}
