package dn.jasm.event;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionEvent extends ApplicationEvent {

    private Long txId;
    private Long userId;
    private Boolean payedAt;
    private BigDecimal totalAmount;
    private Boolean completedAt;


    public TransactionEvent(Object source,
                            Long txId,
                            Long userId,
                            Boolean payedAt,
                            BigDecimal totalAmount,
                            Boolean completedAt) {
        super(source);
        this.txId = txId;
        this.userId = userId;
        this.payedAt = payedAt;
        this.totalAmount = totalAmount;
        this.completedAt = completedAt;

    }
}
