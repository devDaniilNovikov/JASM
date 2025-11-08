package dn.jasm.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import dn.jasm.entity.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(schema = "jasm",name = "transactions")
public class TransactionEntity extends BasedEntity{


    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @JsonBackReference("user-transactions")
    private UserEntity user;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.PERSIST,CascadeType.DETACH})
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonBackReference("card-transactions")
    @JoinColumn(name = "card_id")
    private CardEntity card;

    @OneToOne
    @JoinColumn(name = "order_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonBackReference("order-transaction")
    private OrderEntity orderEntity;

    private Boolean completedAt;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    private BigDecimal totalAmount;






}
