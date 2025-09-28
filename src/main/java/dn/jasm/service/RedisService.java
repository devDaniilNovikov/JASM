package dn.jasm.service;
import java.util.List;
import java.util.Set;

public interface RedisService {

    void writeObjectInRedis(String key, Object object);

    void writeObjectsInRedis(Set<String> keys, Set<Object> objects);

    boolean checkKeyExist(String key);

    boolean checkKeysExist(Set<String> keys);

    void deleteCacheByKey(String key);

    Object getByKey(String key);

    Set<String> getKeySet(String keyPattern);

    void deleteCachesByKeys(Set<String> keys);

    void writeEventInRedis(Class<?> clazz);




}
