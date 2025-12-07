package dn.jasm.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;

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

    @ManyToOne(cascade = {CascadeType.PERSIST,
    CascadeType.DETACH,
    CascadeType.MERGE,
    CascadeType.REFRESH})
    @JoinColumn(name = "user_id")
    @JsonBackReference("user-comments")
    private UserEntity user;

    @ManyToOne(cascade = {CascadeType.PERSIST,
            CascadeType.DETACH,
            CascadeType.MERGE,
            CascadeType.REFRESH})
    @JoinColumn(name = "item_id")
    @JsonBackReference("item-comments")
    private ItemEntity item;

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


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("comment", comment)
                .append("rating", rating)
                .append("user", user)
                .toString();
    }
}
