package dn.jasm.controller;

import dn.jasm.configuration.web.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CookieController {

    private final CookieService cookieService;

    private static final String SET_COOKIES = "/api/v1/cookie/set";
    private static final String GET_COOKIES = "/api/v1/cookie/get";
    private static final String READ_COOKIES = "/api/v1/cookie/read";
    private static final String DELETE_COOKIES = "/api/v1/cookie/delete";


    @PostMapping(SET_COOKIES)
    public void setCookie(@RequestParam String sessionId,
                            HttpServletResponse response){
         cookieService.setCookie(response, sessionId);
    }

    @GetMapping(GET_COOKIES)
    public String getCookies(@RequestParam String sessionId,
                             HttpServletRequest request){
        return cookieService.getCookies(request,sessionId);
    }

    @GetMapping(READ_COOKIES)
    public String readCookies(@RequestParam String sessionId){
        return cookieService.readCookie(sessionId);
    }

    @DeleteMapping(DELETE_COOKIES)
    public void deleteCookies(HttpServletResponse response){
         cookieService.deleteCookie(response);
    }
}
