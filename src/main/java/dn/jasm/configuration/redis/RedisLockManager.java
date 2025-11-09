package dn.jasm.configuration.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockManager {

    private final RedisTemplate<String,Object> redisTemplate;

    private final String script = "HI";



    public String lock(String id,
                       Duration ttl){
        String lockKey = "lock: "+ id;
        String lockId = UUID.randomUUID().toString();
        Boolean isLocked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockId, ttl);
        if (Boolean.TRUE.equals(isLocked)){
            return lockKey;
        }
        return null;
    }

    public void unlock(String id) {
        String lockKey = "lock: " + id;
        Long result = redisTemplate.execute(connection -> connection.scriptingCommands()
                        .eval(script.getBytes(StandardCharsets.UTF_8),
                                ReturnType.INTEGER,
                                1,
                                lockKey.getBytes(StandardCharsets.UTF_8),
                                id.getBytes(StandardCharsets.UTF_8)),
                true);
        if (result != null && result == 1L) {
            log.info("Lock released");
        } else {
            log.info("Lock already lock");
        }
    }

}
