package dn.jasm.event.comment;

import lombok.*;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class CommentEvent extends ApplicationEvent {

    private String comment;
    private Double rating;
    private String createdAt;

    public CommentEvent(Object source,String comment,Double rating,String createdAt) {
        super(source);
        this.comment = comment;
        this.rating = rating;
        this.createdAt = createdAt;
    }
}
