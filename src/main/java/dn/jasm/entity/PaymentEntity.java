package dn.jasm.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import dn.jasm.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(schema = "jasm",name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity {

    @Id
    private String id;



    @ManyToOne(fetch = FetchType.LAZY,cascade = {
            CascadeType.MERGE,
            CascadeType.PERSIST,
            CascadeType.DETACH,
            CascadeType.REFRESH
    })
    @JoinColumn(name = "payment_id")
    @JsonBackReference("card-payments")
    private CardEntity card;

    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;



}
