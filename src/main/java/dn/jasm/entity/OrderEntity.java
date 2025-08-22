package dn.jasm.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import dn.jasm.entity.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(schema = "jasm",name = "order")
@Getter
@Setter
public class OrderEntity extends BasedEntity {

    @ManyToOne(fetch = FetchType.LAZY,cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private UserEntity user;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private Boolean payedAt = false;

    private Double rating;

    private Integer quantityOfItems;


    @OneToMany(mappedBy = "order",fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ItemEntity> items = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "tx_id")
    private TransactionEntity transactionEntity;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        OrderEntity order = (OrderEntity) object;
        return this.getId().equals(order.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "OrderEntity{" +
                "amount=" + amount +
                ", user=" + user +
                ", payedAt=" + payedAt +
                ", rating=" + rating +
                ", items=" + items +
                ", transactionEntity=" + transactionEntity +
                '}';
    }

}
