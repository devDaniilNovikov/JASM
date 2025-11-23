package dn.jasm.service.cache.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dn.jasm.service.cache.CookieCacheService;
import dn.jasm.service.cache.SessionCacheService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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


    private static final String COOKIE_SECRET_KEY = "cookie-key";
    private static final String COOKIE_HTTP_ONLY_AT = "httpOnly";
    private static final String COOKIE_SECURE_AT = "secure";
    private static final String COOKIE_MAX_AGE = "maxAge";
    private static final String COOKIE_PATH = "path";
    private static final String COOKIE_SAMESITE = "sameSite";
    private static final String COOKIE_DOMAIN = "domain";
    private static final String COOKIE_PATH_SYMBOL = "/";
    private static final String COOKIE_NAME = "cookieName";


    @Override
    public Map<String, Object> setCookieForUser(HttpServletResponse response,
                                                HttpSession session) {
        Cookie cookie = new Cookie(cookieName, generateRandomCookieValue(session));
        Map<String, Object> cookieAttributes = new LinkedHashMap<>();
        cookie.setHttpOnly(true);
        cookie.setMaxAge(7 * 24 * 60 * 60);
        cookie.setPath(COOKIE_PATH_SYMBOL);
        cookie.setAttribute(COOKIE_SAMESITE, sameSite);
        cookie.setSecure(isSecure);
        response.addCookie(cookie);
        log.info("Added cookie name is: {},value is: {}",
                cookie.getName(), cookie.getValue());
        cookieAttributes.put(COOKIE_NAME, cookieName);
        cookieAttributes.put(COOKIE_SECRET_KEY, cookie.getValue());
        cookieAttributes.put(COOKIE_HTTP_ONLY_AT, cookie.isHttpOnly());
        cookieAttributes.put(COOKIE_SECURE_AT, cookie.getSecure());
        cookieAttributes.put(COOKIE_MAX_AGE, cookie.getMaxAge());
        cookieAttributes.put(COOKIE_PATH, cookie.getPath());
        cookieAttributes.put(COOKIE_SAMESITE, sameSite);
        cookieAttributes.put(COOKIE_DOMAIN, cookie.getDomain());
        response.setStatus(200);
        log.info("Cookie attributes: {}", cookieAttributes);
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
    public Map<String,Object> getCookieValue(String sessionId,
                                             HttpSession session) {
            Object cookieAttributes = redisTemplate.opsForHash()
                    .get(sessionId, "sessionAttr:cookieAttributes");
            if (cookieAttributes==null){
                log.error("cookieAttributes not found");
                return null;
            }
            final String mapKey = "COOKIE_ATTRIBUTES";
            Map<String,Object> cookieMap = new HashMap<>();
            cookieMap.put(mapKey,cookieAttributes);
            return cookieMap;
    }
}
