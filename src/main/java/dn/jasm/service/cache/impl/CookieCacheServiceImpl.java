package dn.jasm.service.cache.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dn.jasm.service.cache.CookieCacheService;
import dn.jasm.service.cache.SessionCacheService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CookieCacheServiceImpl implements CookieCacheService {

    @Value("${server.servlet.session.cookie.same-site}")
    private String sameSite;

    @Value("${server.servlet.session.cookie.secure}")
    private boolean isSecure;

    @Value("${server.servlet.session.cookie.name}")
    private String cookieName;

    private final RedisTemplate<String, Object> redisTemplate;



    @Override
    public Map<String, Object> setCookieForUser(HttpServletResponse response,
                                                HttpSession session) {
        Cookie cookie = new Cookie(cookieName, generateRandomCookieValue(session));
        cookie.setHttpOnly(true);
        cookie.setMaxAge(7 * 24 * 60 * 60);
        cookie.setPath(CookieAttributes.COOKIE_PATH.getValue());
        cookie.setAttribute(CookieAttributes.COOKIE_SAMESITE.getValue(), sameSite);
        cookie.setSecure(isSecure);
        response.addCookie(cookie);
        log.info("Added cookie name is: {},value is: {}",
                cookie.getName(), cookie.getValue());
        Map<String, Object> cookieAttributes = collectCookieAttributes(cookie);
        response.setStatus(200);
        log.info("Cookie attributes: {}", cookieAttributes);
        return cookieAttributes;
    }

    private Map<String, Object> collectCookieAttributes(Cookie cookie){
        Map<String, Object> cookieAttributes = new LinkedHashMap<>();
        cookieAttributes.put(CookieAttributes.COOKIE_NAME.getValue(), cookieName);
        cookieAttributes.put(CookieAttributes.COOKIE_SECRET_KEY.getValue(), cookie.getValue());
        cookieAttributes.put(CookieAttributes.COOKIE_HTTP_ONLY_AT.getValue(), cookie.isHttpOnly());
        cookieAttributes.put(CookieAttributes.COOKIE_SECURE_AT.getValue(), cookie.getSecure());
        cookieAttributes.put(CookieAttributes.COOKIE_MAX_AGE.getValue(), cookie.getMaxAge());
        cookieAttributes.put(CookieAttributes.COOKIE_PATH.getValue(), cookie.getPath());
        cookieAttributes.put(CookieAttributes.COOKIE_SAMESITE.getValue(), sameSite);
        cookieAttributes.put(CookieAttributes.COOKIE_DOMAIN.getValue(), cookie.getDomain());
        return cookieAttributes;
    }

    @Override
    public String generateRandomCookieValue(HttpSession session) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(session.getId().getBytes());
    }

    @Override
    public void deleteCookie(HttpServletResponse httpServletResponse) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        httpServletResponse.addCookie(cookie);
        httpServletResponse.setStatus(204);

    }

    @Override
    @SneakyThrows
    public Map<String,Object> getCookieValue(String sessionId,
                                             HttpServletResponse response) {

            Object cookieAttributes = redisTemplate.opsForHash()
                    .get(sessionId, CookieAttributes.REDIS_COOKIE_HASH_KEY.getValue()
                    );
            if (cookieAttributes==null){
                log.error("cookieAttributes not found");
                response.sendError(400,"cookieAttributes not found!");
                return null;
            };
            Map<String,Object> cookieMap = new HashMap<>();
            cookieMap.put(CookieAttributes.COOKIE_ATTRIBUTES.getValue(),cookieAttributes);
            response.setStatus(200);
            return cookieMap;
    }
}
