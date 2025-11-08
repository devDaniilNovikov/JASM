package dn.jasm.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema = "jasm",name = "category")
@Getter
@Setter
public class CategoryEntity extends BasedEntity {


    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "category")
    @JsonManagedReference("items-categories")
    private List<ItemEntity> items = new ArrayList<>();


}
