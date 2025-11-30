package dn.jasm.service;
import dn.jasm.configuration.redis.CacheNames;

import java.util.List;
import java.util.Set;

public interface RedisService {

    void writeObjectInRedis(String key, Object object);

    void writeObjectsInRedis(Set<String> keys, Set<Object> objects);

    boolean checkKeyExist(String key);

    boolean checkKeysExist(Set<String> keys);

    void deleteCacheByKey(String key);

    Object getFromCache(String key,CacheNames cacheNames);

    Set<String> getKeySet(String keyPattern);

    void deleteCachesByKeys(Set<String> keys);

    <T >void writeEventInRedis(T t);

    void putToCache(String key, Object value, CacheNames cacheNames);

    List<Object> getObjectsFromRedis(List<String> keys);





}
