package dn.jasm.event;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.UUID;

@Getter
@Setter
public class TicketEvent extends ApplicationEvent implements BaseEvent {

    private String id;

    private String userId;

    private String cardId;

    private String amount;

    private String date;

    private String itemId;

    private String orderId;

    public TicketEvent(Object source,
                       String id,
                       String userId,
                       String cardId,
                       String amount,
                       String itemId,
                       String orderId) {
        super(source);
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.cardId = cardId;
        this.amount = amount;
        this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-Mm-Dd"));
        this.itemId = itemId;
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "TicketEvent{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", cardId='" + cardId + '\'' +
                ", amount='" + amount + '\'' +
                ", date='" + date + '\'' +
                ", itemId='" + itemId + '\'' +
                ", orderId='" + orderId + '\'' +
                '}';
    }

    @Override
    public EventType getEventType() {
        return EventType.TICKET_CREATED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketEvent that = (TicketEvent) o;
        return Objects.equal(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
