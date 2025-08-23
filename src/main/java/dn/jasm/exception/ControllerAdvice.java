package dn.jasm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(UserNotFoundException.class)
    private ResponseEntity<ErrorBody> handleException(WebRequest webRequest, UserNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorBody.builder()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .description(ex.getLocalizedMessage())
                        .path(webRequest.getContextPath())
                        .build());
    }

    @ExceptionHandler(AlreadyExistException.class)
    private ResponseEntity<ErrorBody> handleException(WebRequest webRequest, AlreadyExistException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorBody.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .description(ex.getMessage())
                        .path(webRequest.getDescription(false))
                        .build());
    }


    @ExceptionHandler(Exception.class)
    private ResponseEntity<ErrorBody> handleException(WebRequest webRequest, Exception ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorBody.builder()
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .description(ex.getMessage())
                        .path(webRequest.getDescription(false))
                        .build());
    }
}



