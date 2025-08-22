package dn.jasm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PaymentMethodException extends RuntimeException{

    public PaymentMethodException(String message) {
        super(message);
    }
}
