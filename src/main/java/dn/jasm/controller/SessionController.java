package dn.jasm.controller;

import dn.jasm.configuration.swagger.session.SwaggerAnnotationForHttpSession;
import dn.jasm.configuration.swagger.session.SwaggerAnnotationForHttpSessionCollection;
import dn.jasm.dto.user.SessionKeysRequest;
import dn.jasm.dto.user.UserSessionLoginDto;
import dn.jasm.service.cache.SessionCacheService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.text.MessageFormat;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Session" ,description = "Действия с пользовательскими сессиями")
public class SessionController {

    private final SessionCacheService sessionCacheService;


    private static final String GET_TTL_OF_SESSION = "/api/v1/session/expire";
    private static final String LOGIN = "/api/v1/session/login";
    private static final String LOGOUT = "/api/v1/session/logout";
    private static final String GET_CURRENT_SESSION = "/api/v1/session/current";
    private static final String GET_ALL_ACTIVE_SESSIONS = "/api/v1/session/sessions";
    private static final String EXTEND_SESSION_TIME = "/api/v1/session/time/extend";
    private static final String INVALIDATE_SESSIONS = "/api/v1/session/invalidate";
    private static final String INVALIDATE_ALL_SESSIONS = "/api/v1/session/sessions/invalidate";

    @GetMapping(GET_ALL_ACTIVE_SESSIONS)
    @ResponseStatus(value = HttpStatus.OK)
    @SwaggerAnnotationForHttpSessionCollection(operation = "Получение активных сессий")
    public Set<String> getAllActiveSessions(){
        return sessionCacheService.getAllActiveSessions();
    }



    @PostMapping(LOGIN)
    @ResponseStatus(value = HttpStatus.CREATED)
    @SwaggerAnnotationForHttpSessionCollection(operation = "Вход в сессию")
    public Map<String,Object> login(
            @RequestBody @Valid UserSessionLoginDto userSessionLoginDto,
            HttpSession session,
            HttpServletResponse response
    ){
        return sessionCacheService.login(userSessionLoginDto, session,response);
    }

    @GetMapping(GET_CURRENT_SESSION)
    @ResponseStatus(value = HttpStatus.OK)
    @SwaggerAnnotationForHttpSessionCollection(operation = "Получение текущей сессии")
    public Map<String,Object> getCurrentSession(@RequestBody UserSessionLoginDto userSessionLoginDto,
                                                HttpSession session,
                                                HttpServletResponse response){
        return sessionCacheService.getCurrentSession(userSessionLoginDto,session,response);
    }

    @PostMapping(LOGOUT)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @SwaggerAnnotationForHttpSession(operation = "Выход из сессии")
    public void logout(HttpSession session,
                         @RequestParam String sessionId,
                         HttpServletResponse response){
        if (sessionId.equals(session.getId())) {
            sessionCacheService.invalidateSession(session.getId(),response);
            session.invalidate();
            response.setStatus(200);
             log.info("Session: {} removed from Redis",sessionId);
        }
        else {
            response.setStatus(400);
            throw new IllegalArgumentException(
                    MessageFormat.format("Wrong session id: {0}",sessionId)
            );
        }
    }

    @PostMapping(EXTEND_SESSION_TIME)
    @ResponseStatus(value = HttpStatus.OK)
    @SwaggerAnnotationForHttpSession(operation = "Увеличение активного времени сессии")
    public void extendSessionTime(@RequestParam String sessionId,
                                  @RequestParam long minutes){
        sessionCacheService.extendSessionTime(sessionId,minutes);
    }

    @PostMapping(INVALIDATE_SESSIONS)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @SwaggerAnnotationForHttpSession(operation = "Инвалидация сессий по их ключам")
    public void invalidateSessionsByKeys(@RequestBody SessionKeysRequest sessionKeysRequest,
                                         HttpServletResponse response){
        sessionCacheService.invalidateSessions(sessionKeysRequest,response);
    }

    @PostMapping(INVALIDATE_ALL_SESSIONS)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @SwaggerAnnotationForHttpSession(operation = "Инвалидация всех сессий")
    public void invalidateAllSessions(@RequestParam String redisKeysPrefix,
                                      HttpServletResponse response){
        sessionCacheService.invalidateSessions(redisKeysPrefix,response);
    }

    @GetMapping(GET_TTL_OF_SESSION)
    @ResponseStatus(value = HttpStatus.OK)
    @SwaggerAnnotationForHttpSession(operation = "Получение времени жизни сессии")
    public String getTtlOfSession(@RequestParam String sessionId,
                                  HttpServletResponse response){
        return sessionCacheService.getTtlOfSession(sessionId,response);
    }




}
