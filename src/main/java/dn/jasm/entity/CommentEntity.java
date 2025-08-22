package dn.jasm.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(schema = "jasm",name = "comment")
@Getter
@Setter
public class CommentEntity extends BasedEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Column(nullable = false)
    private String comment;

    @Column(nullable = true)
    private Double rating;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private UserEntity user;

    @Override
    public boolean equals(Object object) {
        if (this==object) return true;
        if (object ==null || getClass() != object.getClass()) return false;
        CommentEntity commentEntity = (CommentEntity) object;
        return this.getId().equals(commentEntity.getId());
    }



    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }
}
