package dn.jasm.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import dn.jasm.entity.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(schema = "jasm",name = "transactions")
public class TransactionEntity extends BasedEntity {



    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference("user-transaction")
    private UserEntity userEntity;

    @OneToOne
    @JoinColumn(name = "order_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private OrderEntity orderEntity;

    private Boolean completedAt;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Override
    public String toString() {
        return "TransactionEntity{" +
                "userEntity=" + userEntity +
                ", orderEntity=" + orderEntity +
                ", completedAt=" + completedAt +
                ", transactionStatus=" + transactionStatus +
                '}';
    }
}
