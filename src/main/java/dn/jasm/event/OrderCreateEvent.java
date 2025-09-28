package dn.jasm.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class OrderCreateEvent extends ApplicationEvent implements BaseEvent {

    private Long orderId;
    private Boolean payedAt;
    private String status;
    private BigDecimal totalAmount;
    private Boolean isShipped;
    private List<Long> itemsIds;

    public OrderCreateEvent(Object source,
                            Long orderId,
                            Boolean payedAt,
                            String status,
                            BigDecimal totalAmount,
                            Boolean isShipped,
                            List<Long> itemsIds) {
        super(source);
        this.orderId = orderId;
        this.payedAt = payedAt;
        this.status = status;
        this.totalAmount = totalAmount;
        this.isShipped = isShipped;
        this.itemsIds = new ArrayList<>(itemsIds.size());
    }


    @Override
    public String toString() {
        return "OrderCreateEvent{" +
                "orderId=" + orderId +
                ", payedAt=" + payedAt +
                ", status='" + status + '\'' +
                ", totalAmount=" + totalAmount +
                ", isShipped=" + isShipped +
                '}';
    }

    @Override
    public EventType getEventType() {
        return EventType.ORDER_CREATE;
    }
}
