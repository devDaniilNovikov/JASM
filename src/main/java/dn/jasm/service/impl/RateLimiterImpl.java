package dn.jasm.service.impl;
import dn.jasm.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RateLimiterImpl implements RateLimiterService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean allowRequest(String clientId,
                                int limit,
                                Duration timeOut){
        long windowIndex = System.currentTimeMillis() / timeOut.toMillis();
        String key = String.format("rate:%s%s",clientId,windowIndex);
        Long countHints = redisTemplate.opsForValue()
                .increment(key);
        if (countHints!=null && countHints ==1L){
               redisTemplate.expire(key,timeOut);
        }

        return countHints!=null && countHints <=limit;
    }

    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @RequiredArgsConstructor
    public static class RateLimiterFilter extends OncePerRequestFilter {


        private final RateLimiterService rateLimiterService;

        private static final String CLIENT_HEADER = "X-API-KEY";
        private static final String UNKNOWN_CLIENT = "unknown";
        private static final int    LIMIT_VALUE = 10;
        private static final String ERROR_MESSAGE = "Rate limit exceed!";

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            String client = Optional.ofNullable(request.getHeader(CLIENT_HEADER))
                            .filter(s->!s.isBlank())
                            .orElseGet(()->Optional.ofNullable(request.getRemoteAddr())
                            .orElse(UNKNOWN_CLIENT));
            boolean allowed = rateLimiterService.allowRequest(client,
                    LIMIT_VALUE,
                    Duration.ofMinutes(1));
            if (!allowed){
                response.setStatus(429);
                response.getWriter().println(ERROR_MESSAGE);
                return;
            }
            filterChain.doFilter(request,response);

        }
    }

}
