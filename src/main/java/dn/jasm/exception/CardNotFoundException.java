package dn.jasm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException() {
    }

    public CardNotFoundException(String message) {
        super(message);
    }
}
