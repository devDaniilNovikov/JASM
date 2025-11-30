package dn.jasm.service.cache.impl;

import dn.jasm.dto.user.SessionKeysRequest;
import dn.jasm.dto.user.UserSessionLoginDto;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.repository.UserRepository;
import dn.jasm.service.cache.SessionCacheService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionCacheServiceImpl implements SessionCacheService {


    private final UserRepository userRepository;
    private final RedisIndexedSessionRepository sessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    public void extendSessionTime(String sessionId,
                                  long minutes){
        redisTemplate.expire(sessionId, minutes, TimeUnit.MINUTES);
    }

    @Override
    public Set<String> getAllActiveSessions(){
        return redisTemplate.keys(SessionAttributes.SESSION_PREFIX
                        .getValue()
                        .concat(SessionAttributes.REDIS_KEYS_PREFIX
                        .getValue()));
    }

    @Override
    public void invalidateSession(String sessionId,
                                  HttpServletResponse response){
        sessionRepository.deleteById(sessionId);
        redisTemplate.delete(sessionId);
    }

    @SneakyThrows
    @Override
    public Map<String, Object> login(UserSessionLoginDto userSessionLoginDto,
                                     HttpSession session,
                                     HttpServletResponse response) {

        session.setAttribute(SessionAttributes.USER_ID.getValue(),
                userSessionLoginDto.userId());
        session.setAttribute(SessionAttributes.USERNAME.getValue(),
                userSessionLoginDto.username());
        session.setAttribute(SessionAttributes.LOGIN_TIME.getValue(),
                System.currentTimeMillis());
        session.setAttribute(SessionAttributes.SESSION_CREATION_TIME.getValue(),
                session.getCreationTime());
        session.setAttribute(SessionAttributes.MAX_INACTIVE_INTERVAL.getValue(),
                session.getMaxInactiveInterval());
        if (session.getId()==null){
            response.sendError(400,"Session id is null!!");
        }
        response.setStatus(200);
        return Map.of(
                SessionAttributes.SESSION_ID.getValue(),session.getId(),
                SessionAttributes.USER_ID.getValue(),userSessionLoginDto.userId()
        );
    }

    @Override
    public Map<String,Object> getCurrentSession(UserSessionLoginDto userSessionLoginDto,
                                                HttpSession session,
                                                HttpServletResponse response){
        var requireUser = userRepository.findById(userSessionLoginDto.userId())
                .filter(userEntity -> userEntity.getUsername()
                        .equals(userSessionLoginDto.username()))
                .orElseThrow(UserNotFoundException::new);
        Map<String,Object> sessionMap = new HashMap<>();
        sessionMap.put(SessionAttributes.SESSION_ID.getValue(),session.getId());
        sessionMap.put(SessionAttributes.USER_ID.getValue(),requireUser.getId());
        sessionMap.put(SessionAttributes.USERNAME.getValue(),requireUser.getUsername());
        sessionMap.put(SessionAttributes.LOGIN_TIME.getValue(),System.currentTimeMillis());
        response.setStatus(200);
        Map<String,Object> updatedSession = new HashMap<>(sessionMap);
        updatedSession.remove(SessionAttributes.USERNAME.getValue());
        updatedSession.remove(SessionAttributes.LOGIN_TIME.getValue());
        return updatedSession;

    }


    @Override
    public void invalidateSessions(SessionKeysRequest sessionKeysRequest,
                                   HttpServletResponse response) {
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
    public void invalidateSessions(String redisKeysPrefix,
                                   HttpServletResponse response) {
        redisKeysPrefix = SessionAttributes.REDIS_KEYS_PREFIX.getValue();
        redisTemplate.keys(redisKeysPrefix).forEach(redisTemplate::delete);
    }

    @Override
    public String getTtlOfSession(String sessionId,
                                  HttpServletResponse response) {
        var ttl = redisTemplate.getExpire(sessionId);
        log.info("EXPIRE OF SESSION: {} IS {}",sessionId,ttl);
        return String.valueOf(ttl);
    }

    @Override
    public String getSessionId(HttpSession httpSession,
                               HttpServletResponse response){
        return httpSession.getId();
    }


}

