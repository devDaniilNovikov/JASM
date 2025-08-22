package dn.jasm.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema = "jasm",name = "warehouse")
@Getter
@Setter
public class WareHouseEntity extends BasedEntity {


    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id")
    private LocationEntity location;

    @OneToMany(mappedBy = "wareHouse")
    private List<ItemEntity> items = new ArrayList<>();
}
