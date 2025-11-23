package dn.jasm.service.cache;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.*;

public interface CookieCacheService {

    Map<String, Object> setCookieForUser(HttpServletResponse httpServletResponse,
                                               HttpSession httpSession);

    String generateRandomCookieValue(HttpSession httpSession);

    void deleteCookie(HttpServletResponse httpServletResponse);

    String getCookieValue(String cookieName);
}
