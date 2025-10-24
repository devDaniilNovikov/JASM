package dn.jasm.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
public class









































PaymentEvent extends ApplicationEvent implements BaseEvent  {

    private BigDecimal amount;;
    private String email;
    private String cardNumber;

    public PaymentEvent(Object source,
                        BigDecimal amount,
                        String email,
                        String cardNumber) {
        super(source);
        this.amount = amount;
        this.email = email;
        this.cardNumber = cardNumber;
    }

    @Override
    public EventType getEventType() {
        return EventType.PAYMENT_COMPLETED;
    }
}
