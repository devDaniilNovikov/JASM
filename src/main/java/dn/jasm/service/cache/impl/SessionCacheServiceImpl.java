package dn.jasm.service.cache.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import dn.jasm.dto.user.SessionKeysRequest;
import dn.jasm.dto.user.UserSessionLoginDto;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.repository.UserRepository;
import dn.jasm.service.cache.SessionCacheService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionCacheServiceImpl implements SessionCacheService {

    private static final String SESSION_PREFIX = "spring:session:sessions";
    private static final String SESSION_ID = "sessionId";
    private static final String USER_ID = "userId";
    private static final String USERNAME = "username";
    private static final String LOGIN_TIME = "loginTime";
    private static final String SESSION_CREATION_TIME = "sessionCreationTime";
    private static final String MAX_INACTIVE_INTERVAL = "MaxInActiveInterval";
    private static final String REDIS_KEYS_PREFIX = "*";

    private final UserRepository userRepository;
    private final RedisIndexedSessionRepository sessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void extendSessionTime(String sessionId,
                                  long seconds){
        redisTemplate.expire(sessionId,
                seconds,
                TimeUnit.SECONDS
        );
    }

    @Override
    public Set<String> getAllActiveSessions(){
        return redisTemplate.keys(SESSION_PREFIX+REDIS_KEYS_PREFIX);
    }

    @Override
    public void invalidateSession(String sessionId){
        String key = SESSION_PREFIX+sessionId;
        var session = redisTemplate.opsForValue().get(key);
        var newSession = sessionRepository.createSession();
        log.info("Session: {}",session);
        redisTemplate.delete(key);
    }

    @Override
    public Map<String, Object> login(UserSessionLoginDto userSessionLoginDto,
                                     HttpSession session) {

        session.setAttribute(USER_ID,userSessionLoginDto.userId());
        session.setAttribute(USERNAME,userSessionLoginDto.username());
        session.setAttribute(LOGIN_TIME,System.currentTimeMillis());
        session.setAttribute(SESSION_CREATION_TIME,session.getCreationTime());
        session.setAttribute(MAX_INACTIVE_INTERVAL,session.getMaxInactiveInterval());
        return Map.of(
                SESSION_ID,session.getId(),
                "message","Session was save in redis");
    }

    @Override
    public Map<String,Object> getCurrentSession(UserSessionLoginDto userSessionLoginDto,
                                                HttpSession session){
        var requireUser = userRepository.findById(userSessionLoginDto.userId())
                .filter(userEntity -> userEntity.getUsername()
                        .equals(userSessionLoginDto.username()))
                .orElseThrow(UserNotFoundException::new);
        Map<String,Object> sessionMap = new HashMap<>();
        sessionMap.put(SESSION_ID,session.getId());
        sessionMap.put(USER_ID,requireUser.getId());
        sessionMap.put(USERNAME,requireUser.getUsername());
        sessionMap.put(LOGIN_TIME,System.currentTimeMillis());
        Map<String,Object> updatedSession = new HashMap<>(sessionMap);
        updatedSession.remove(USERNAME);
        updatedSession.remove(LOGIN_TIME);
        return updatedSession;

    }


    @Override
    public void invalidateSessionsByKeys(SessionKeysRequest sessionKeysRequest) {
        var keyCount = sessionKeysRequest.keys().size();
        boolean existAt = redisTemplate.countExistingKeys(
                sessionKeysRequest.keys())!=keyCount;
         if (existAt) {
            throw new IllegalArgumentException("Keys not found");
        }
        sessionKeysRequest.keys()
                .stream()
                .filter(Objects::nonNull)
                .filter(redisTemplate::hasKey)
                .forEach(key->{
                    redisTemplate.delete(sessionKeysRequest.keys());
                    log.info("Deleted keys: {}",redisTemplate.keys(key));
                });
    }



    @Override
    @Transactional
    public void invalidateAllSessions(String redisKeysPrefix) {
        redisKeysPrefix = REDIS_KEYS_PREFIX;
        redisTemplate.keys(redisKeysPrefix).forEach(redisTemplate::delete);
    }



}

