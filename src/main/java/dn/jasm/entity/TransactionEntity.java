package dn.jasm.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(schema = "jasm",name = "transactions")
public class TransactionEntity extends BasedEntity {

    @OneToOne
    @JoinColumn(name = "payment_id",nullable = false,updatable = false)
    private PaymentEntity paymentEntity;


}
