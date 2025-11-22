package dn.jasm.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(schema = "jasm",name = "item")
@Getter
@Setter
public class ItemEntity extends BasedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonBackReference("order-items")
    private OrderEntity order;

    @Column(nullable = false,unique = true)
    private String name;

    @CollectionTable
    private List<String> photoUrls = new ArrayList<>();

    private BigDecimal price;

    private String description;

    private String type;

    private Double rating;

    @ManyToOne(fetch = FetchType.LAZY,cascade = {
            CascadeType.MERGE,
            CascadeType.DETACH,
            CascadeType.PERSIST,
            CascadeType.REFRESH}
    )
    @JoinColumn(name = "category_id")
    @JsonBackReference("items-categories")
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY,cascade = {
            CascadeType.MERGE,
            CascadeType.DETACH,
            CascadeType.PERSIST,
            CascadeType.REFRESH}
    )
    @JoinColumn(name = "warehouse_id")
    private StorageEntity wareHouse;

    private Integer discount;

    private Boolean isShippable;

    private Integer quantity;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ItemEntity item = (ItemEntity) object;
        return this.getId().equals(item.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "ItemEntity{" +
                ", name='" + name + '\'' +
                ", photoUrls=" + photoUrls +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", rating=" + rating +
                ", category=" + category +
                ", wareHouse=" + wareHouse +
                ", discount=" + discount +
                ", isShippable=" + isShippable +
                ", quantity=" + quantity +
                '}';
    }
}
