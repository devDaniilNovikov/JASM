package dn.jasm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RedisKeyException extends RuntimeException{

    public RedisKeyException(String message) {
        super(message);
    }

    public RedisKeyException() {
    }
}
