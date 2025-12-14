package dn.jasm.controller;

import dn.jasm.configuration.swagger.cookie.SwaggerAnnotationForCookie;
import dn.jasm.configuration.web.CookieService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Cookie" ,description = "Действия с куки")
public class CookieController {

    private final CookieService cookieService;

    private static final String SET_COOKIES = "/api/v1/cookies/set";
    private static final String GET_COOKIES = "/api/v1/cookies/get";
    private static final String READ_COOKIES = "/api/v1/cookies/read";
    private static final String DELETE_COOKIES = "/api/v1/cookies/delete";


    @PostMapping(SET_COOKIES)
    @ResponseStatus(HttpStatus.CREATED)
    @SwaggerAnnotationForCookie(operation = "Cоздание куки")
    public void setCookie(@RequestParam String sessionId,
                            HttpServletResponse response){
         cookieService.setCookie(response, sessionId);
    }

    @GetMapping(GET_COOKIES)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForCookie(operation = "Получение куки")
    public String getCookies(@RequestParam String sessionId,
                             HttpServletRequest request){
        return cookieService.getCookies(request,sessionId);
    }

    @GetMapping(READ_COOKIES)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForCookie(operation = "Чтение куки")
    public String readCookies(@RequestParam String sessionId){
        return cookieService.readCookie(sessionId);
    }

    @DeleteMapping(DELETE_COOKIES)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SwaggerAnnotationForCookie(operation = "Удаление куков")
    public void deleteCookies(HttpServletResponse response){
         cookieService.deleteCookie(response);
    }
}
