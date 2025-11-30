package dn.jasm.configuration.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CookieService {

    private final RedisIndexedSessionRepository sessionRepository;
    private final RedisTemplate<String,Object> redisTemplate;

    private static final String COOKIE_PREFIX = "cookie:";

    public void setCookie(HttpServletResponse response,
                            String sessionId) {
        if (sessionRepository.findById(sessionId) != null) {
            Cookie cookie = new Cookie(sessionId, UUID.randomUUID().toString());
            cookie.setPath("/");
            cookie.setMaxAge(7 * 7 * 24 * 60);
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
            response.setStatus(201);
            redisTemplate.opsForValue()
                    .set(COOKIE_PREFIX+sessionId,
                    cookie.getValue(),
                    Duration.ofMinutes(30));
             log.info("Cookie with name: {} is install!",sessionId);
        }
    }

    public String getCookies(HttpServletRequest request,
                             String sessionId){
        Cookie[] cookies = request.getCookies();
        if (cookies!=null){
            for (Cookie cookie:cookies){
                if (sessionId.equals(cookie.getName())){
                    return cookie.getValue();
                }
            }
        }
        return MessageFormat.format("Cookie with name: {0} not found",sessionId);
    }

    public String readCookie(String sessionId){
        return MessageFormat.format("Session ID: {0}",sessionId);
    }

    public void deleteCookie(HttpServletResponse response){
        Cookie cookie = new Cookie("sessionId",null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        log.info("Cookie with name: {} delete",cookie.getName());
    }
}
