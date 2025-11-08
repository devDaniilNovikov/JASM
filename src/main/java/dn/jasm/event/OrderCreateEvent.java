package dn.jasm.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonMerge;
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
    private List<String> itemNames;

    public OrderCreateEvent(Object source,
                            Long orderId,
                            Boolean payedAt,
                            String status,
                            BigDecimal totalAmount,
                            Boolean isShipped,
                            List<String> itemNames) {
        super(source);
        this.orderId = orderId;
        this.payedAt = payedAt;
        this.status = status;
        this.totalAmount = totalAmount;
        this.isShipped = isShipped;
        this.itemNames = itemNames;
    }


    @Override
    public String toString() {
        return "OrderCreateEvent{" +
                "orderId=" + orderId +
                ", payedAt=" + payedAt +
                ", status='" + status + '\'' +
                ", totalAmount=" + totalAmount +
                ", isShipped=" + isShipped +
                ", itemNames=" + itemNames +
                '}';
    }

    @Override
    public EventType getEventType() {
        return EventType.ORDER_CREATE;
    }
}
