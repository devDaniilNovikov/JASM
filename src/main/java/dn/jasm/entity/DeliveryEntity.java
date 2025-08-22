package dn.jasm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = "jasm",name = "delivery")
@Getter
@Setter
public class DeliveryEntity extends BasedEntity {

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id")
    private LocationEntity location;
}
