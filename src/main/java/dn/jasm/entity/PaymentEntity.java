package dn.jasm.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(schema = "jasm",name = "payment")
@Getter
@Setter
public class PaymentEntity extends BasedEntity {



    @ManyToOne(fetch = FetchType.LAZY,cascade = {
            CascadeType.MERGE,
            CascadeType.PERSIST,
            CascadeType.DETACH,
            CascadeType.REFRESH
    })
    @JoinColumn(name = "payment_id")
    private CardEntity card;

    private BigDecimal amount;

    private String currency;

}
