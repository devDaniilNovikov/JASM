package dn.jasm.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentEvent extends ApplicationEvent {

    private BigDecimal amount;
    private String paymentMethod;
    private String email;
    private String cardNumber;

    public PaymentEvent(Object source,
                        BigDecimal amount,
                        String paymentMethod,
                        String email,
                        String cardNumber) {
        super(source);
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.email = email;
        this.cardNumber = cardNumber;
    }
}
