package dn.jasm.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema = "jasm",name = "location")
@Getter
@Setter
public class LocationEntity extends BasedEntity {


    @OneToMany(mappedBy = "location")
    private List<DeliveryEntity> deliveries = new ArrayList<>();

    @OneToMany(mappedBy = "location")
    private List<WareHouseEntity> wareHouses = new ArrayList<>();
}
