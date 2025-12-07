package dn.jasm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FAILED_DEPENDENCY)
public class ApplicationStartRunningException extends RuntimeException{

    public ApplicationStartRunningException(String message) {
        super(message);
    }
}
