package dn.jasm.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class CardCreateEvent extends ApplicationEvent {

    private String cardNumber;
    private String date;
    private String cvc;
    private String id;

    public CardCreateEvent(Object source,
                           String cardNumber,
                           String date,
                           String cvc,
                           String id) {
        super(source);
        this.cardNumber = cardNumber;
        this.date = date;
        this.cvc ="#"+cvc;
        this.id = id;
    }

    @Override
    public String toString() {
        return "CardCreateEvent{" +
                "cardNumber='" + cardNumber + '\'' +
                ", date='" + date + '\'' +
                ", cvc='" + cvc + '\'' +
                '}';
    }
}
