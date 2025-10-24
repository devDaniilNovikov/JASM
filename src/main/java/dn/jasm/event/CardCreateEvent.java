package dn.jasm.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class CardCreateEvent extends ApplicationEvent implements BaseEvent {

    private String cardNumber;
    private Long expMonth;
    private Long expYear;
    private String cvc;
    private String id;

    public CardCreateEvent(Object source,
                           String cardNumber,
                           Long expMonth,
                           Long expYear,
                           String cvc,
                           String id) {
        super(source);
        this.cardNumber = cardNumber;
        this.expMonth = expMonth;
        this.expYear = expYear;
        this.cvc = cvc;
        this.id = id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CardCreateEvent{");
        sb.append("cardNumber='").append(cardNumber).append('\'');
        sb.append(", expMonth=").append(expMonth);
        sb.append(", expYear=").append(expYear);
        sb.append(", cvc='").append(cvc).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public EventType getEventType() {
        return EventType.USER_CREATE;
    }
}
