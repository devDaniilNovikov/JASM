package dn.jasm.configuration.redis;
import java.util.List;
import java.util.Set;

public interface RedisService {

    void writeObjectInRedis(String key, Object object);

    void writeObjectsInRedis(List<String> keys, List<Object> objects);

    boolean checkKeyExist(String key);

    boolean checkKeysExist(List<String> keys);

    void deleteCacheByKey(String key);

    Object getByKey(String key);

    Set<String> getKeySet(String keyPattern);

    void deleteCachesByKeys(List<String> keys);




}
