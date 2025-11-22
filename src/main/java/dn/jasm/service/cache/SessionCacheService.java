package dn.jasm.service.cache;

import dn.jasm.dto.user.SessionKeysRequest;
import dn.jasm.dto.user.UserSessionLoginDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;

import java.util.Map;
import java.util.Set;

public interface SessionCacheService {

    void extendSessionTime(String sessionId,
                           long minutes);

    Set<String> getAllActiveSessions();

    void invalidateSession(String sessionId);

    Map<String,Object> login(UserSessionLoginDto userSessionLoginDto,
                             HttpSession session);

    Map<String,Object> getCurrentSession(UserSessionLoginDto userSessionLoginDto,
                                         HttpSession session);

    void invalidateSessionsByKeys(SessionKeysRequest sessionKeysRequest);

    void invalidateAllSessions(String redisKeysPrefix);


}
