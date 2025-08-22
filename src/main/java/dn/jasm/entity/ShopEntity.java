package dn.jasm.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = "jasm",name = "shop")
@Getter
@Setter
public class ShopEntity extends BasedEntity {

    private Long countOfDeals = 0L;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String ownerName;

    private Double rating;



}
