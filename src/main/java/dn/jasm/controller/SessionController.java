package dn.jasm.controller;

import dn.jasm.dto.user.SessionKeysRequest;
import dn.jasm.dto.user.UserSessionLoginDto;
import dn.jasm.service.cache.SessionCacheService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SessionController {

    private final SessionCacheService sessionCacheService;



    private static final String LOGIN = "/api/v1/session/login";
    private static final String LOGOUT = "/api/v1/session/logout";
    private static final String GET_CURRENT_SESSION = "/api/v1/session/current";
    private static final String GET_ALL_ACTIVE_SESSIONS = "/api/v1/session/sessions";
    private static final String EXTEND_SESSION_TIME = "/api/v1/session/time/extend";
    private static final String INVALIDATE_SESSIONS = "/api/v1/session/invalidate";
    private static final String INVALIDATE_ALL_SESSIONS = "/api/v1/session/sessions/invalidate";

    @GetMapping(GET_ALL_ACTIVE_SESSIONS)
    public Set<String> getAllActiveSessions(){
        return sessionCacheService.getAllActiveSessions();
    }



    @PostMapping(LOGIN)
    public Map<String,Object> login(@RequestBody UserSessionLoginDto userSessionLoginDto,
                                    HttpSession session){
        return sessionCacheService.login(userSessionLoginDto, session);
    }

    @GetMapping(GET_CURRENT_SESSION)
    public Map<String,Object> getCurrentSession(@RequestBody UserSessionLoginDto userSessionLoginDto,
                                                HttpSession session){
        return sessionCacheService.getCurrentSession(userSessionLoginDto,session);
    }

    @PostMapping(LOGOUT)
    public String logout(HttpSession session){
        sessionCacheService.invalidateSession(session.getId());
        session.invalidate();
        return "Session removed from Redis";
    }

    @PostMapping(EXTEND_SESSION_TIME)
    public void extendSessionTime(@RequestParam String sessionId,
                                  @RequestParam long seconds){
        sessionCacheService.extendSessionTime(sessionId,seconds);
    }

    @PostMapping(INVALIDATE_SESSIONS)
    public void invalidateSessionsByKeys(@RequestBody SessionKeysRequest sessionKeysRequest){
        sessionCacheService.invalidateSessionsByKeys(sessionKeysRequest);
    }

    @PostMapping(INVALIDATE_ALL_SESSIONS)
    public void invalidateAllSessions(@RequestParam String redisKeysPrefix){
        sessionCacheService.invalidateAllSessions(redisKeysPrefix);
    }



}
