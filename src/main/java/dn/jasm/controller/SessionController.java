package dn.jasm.controller;

import dn.jasm.dto.user.SessionKeysRequest;
import dn.jasm.dto.user.UserSessionLoginDto;
import dn.jasm.service.cache.CookieCacheService;
import dn.jasm.service.cache.SessionCacheService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.MessageFormat;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SessionController {

    private final SessionCacheService sessionCacheService;
    private final CookieCacheService cookieCacheService;


    private static final String GET_TTL_OF_SESSION = "/api/v1/session/expire";
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
    public Map<String,Object> login(
            @RequestBody @Valid UserSessionLoginDto userSessionLoginDto,
            HttpSession session,
            HttpServletResponse response
    ){
        return sessionCacheService.login(userSessionLoginDto, session,response);
    }

    @GetMapping(GET_CURRENT_SESSION)
    public Map<String,Object> getCurrentSession(@RequestBody UserSessionLoginDto userSessionLoginDto,
                                                HttpSession session,
                                                HttpServletResponse response){
        return sessionCacheService.getCurrentSession(userSessionLoginDto,session,response);
    }

    @PostMapping(LOGOUT)
    public String logout(HttpSession session,
                         @RequestParam String sessionId,
                         HttpServletResponse response){
        if (sessionId.equals(session.getId())) {
            sessionCacheService.invalidateSession(session.getId(),response);
            session.invalidate();
            response.setStatus(200);
            return "Session removed from Redis";
        }
        else {
            response.setStatus(400);
            throw new IllegalArgumentException(
                    MessageFormat.format("Wrong session id: {0}",sessionId)
            );
        }
    }

    @PostMapping(EXTEND_SESSION_TIME)
    public void extendSessionTime(@RequestParam String sessionId,
                                  @RequestParam long minutes){
        sessionCacheService.extendSessionTime(sessionId,minutes);
    }

    @PostMapping(INVALIDATE_SESSIONS)
    public void invalidateSessionsByKeys(@RequestBody SessionKeysRequest sessionKeysRequest,
                                         HttpServletResponse response){
        sessionCacheService.invalidateSessions(sessionKeysRequest,response);
    }

    @PostMapping(INVALIDATE_ALL_SESSIONS)
    public void invalidateAllSessions(@RequestParam String redisKeysPrefix,
                                      HttpServletResponse response){
        sessionCacheService.invalidateSessions(redisKeysPrefix,response);
    }

    @GetMapping(GET_TTL_OF_SESSION)
    public String getTtlOfSession(@RequestParam String sessionId,
                                  HttpServletResponse response){
        return sessionCacheService.getTtlOfSession(sessionId,response);
    }

    @GetMapping("/api/v1/session/cookies/value")
    public Map<String,Object> getCookieValue(@RequestParam String sessionId,
                                             HttpServletResponse response){
        return cookieCacheService.getCookieValue(sessionId,response);
    }



}
