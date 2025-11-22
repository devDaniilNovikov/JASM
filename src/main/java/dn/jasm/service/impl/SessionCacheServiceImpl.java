package dn.jasm.service.impl;

import dn.jasm.dto.user.SessionKeysRequest;
import dn.jasm.dto.user.UserSessionLoginDto;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.repository.UserRepository;
import dn.jasm.service.SessionCacheService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
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

    private final RedisTemplate<String, Object> sessionRedisTemplate;
    private final UserRepository userRepository;

    @Override
    public void extendSessionTime(String sessionId,
                                  long seconds){
        sessionRedisTemplate.expire(sessionId,
                seconds,
                TimeUnit.SECONDS
        );
    }

    @Override
    public Set<String> getAllActiveSessions(){
        return sessionRedisTemplate.keys(SESSION_PREFIX+REDIS_KEYS_PREFIX);
    }

    @Override
    public void invalidateSession(String sessionId){
        String key = SESSION_PREFIX+sessionId;
        var session = sessionRedisTemplate.opsForValue().get(key);
        log.info("Session: {}",session);
        sessionRedisTemplate.delete(key);
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
    public Map<String,Object> getCurrentSession(Long userId,
                                                HttpSession session,
                                                String username){
        var requireUser = userRepository.findById(userId)
                .filter(userEntity -> userEntity.getUsername().equals(username))
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
        sessionRedisTemplate.delete(sessionKeysRequest.keys());
    }


}

