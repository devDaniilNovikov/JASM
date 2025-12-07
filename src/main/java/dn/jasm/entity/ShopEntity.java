package dn.jasm.entity;


import com.fasterxml.jackson.annotation.*;
import dn.jasm.dto.payment.PaymentResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(schema = "jasm",name = "shop")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class ShopEntity extends BasedEntity implements Serializable {


    @Column(unique = true)
    private String name;

    private String category;

    private Integer countOfSales;

    @Column(unique = true,nullable = false)
    private String ownerName;

    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            mappedBy = "shop")
    @JsonManagedReference("shop-items")
    private List<ItemEntity> items;

    private Double rating;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference("user-shops")
    private UserEntity user;

    private BigDecimal deposit;

    private BigDecimal totalCashTurnover;

    private Integer totalCountOfProducts;

    private Integer reviewCounts;

//    @Column(nullable = false)
    private Boolean isVerified;

//    @Column(nullable = false)
    private Boolean isActive;

    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            mappedBy = "shop")
    @JsonManagedReference("shop-orders")
    private List<OrderEntity> orders;

    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            mappedBy = "shop")
    @JsonManagedReference("shop-payments")
    private List<PaymentEntity> payments;

    private String location;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ShopEntity shop = (ShopEntity) o;
        return Objects.equals(name, shop.name) &&
                Objects.equals(shop.getId(),getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
