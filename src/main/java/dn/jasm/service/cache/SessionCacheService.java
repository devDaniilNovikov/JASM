package dn.jasm.service.cache;

import dn.jasm.dto.user.SessionKeysRequest;
import dn.jasm.dto.user.UserSessionLoginDto;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;

import java.util.Map;
import java.util.Set;

public interface SessionCacheService {

    void extendSessionTime(String sessionId,
                           long minutes);

    Set<String> getAllActiveSessions();

    void invalidateSession(String sessionId,
                           HttpServletResponse response);

    Map<String,Object> login(UserSessionLoginDto userSessionLoginDto,
                             HttpSession session,
                             HttpServletResponse response);

    Map<String,Object> getCurrentSession(UserSessionLoginDto userSessionLoginDto,
                                         HttpSession session,
                                         HttpServletResponse response);

    void invalidateSessions(SessionKeysRequest sessionKeysRequest,
                            HttpServletResponse response);

    void invalidateSessions(String redisKeysPrefix,
                            HttpServletResponse response);

    String getTtlOfSession(String sessionId,
                           HttpServletResponse response);

    String getSessionId(HttpSession httpSession,
                        HttpServletResponse response);



}
