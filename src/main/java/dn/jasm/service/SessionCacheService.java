package dn.jasm.service;

import dn.jasm.dto.user.SessionKeysRequest;
import dn.jasm.dto.user.UserSessionLoginDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Set;

public interface SessionCacheService {

    void extendSessionTime(String sessionId,
                           long minutes);

    Set<String> getAllActiveSessions();

    void invalidateSession(String sessionId);

    Map<String,Object> login(UserSessionLoginDto userSessionLoginDto,
                             HttpSession session);

    Map<String,Object> getCurrentSession(Long userId,
                                         HttpSession session,
                                         String username);

    void invalidateSessionsByKeys(SessionKeysRequest sessionKeysRequest);

}
