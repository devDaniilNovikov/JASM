package dn.jasm.event;

import lombok.*;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentEvent extends ApplicationEvent {

    private String comment;
    private Double rating;
    private LocalDateTime createdAt;

    public CommentEvent(Object source,String comment,Double rating,LocalDateTime createdAt) {
        super(source);
        this.comment = comment;
        this.rating = rating;
        this.createdAt = createdAt;
    }
}
