package dn.jasm.event.comment;

import dn.jasm.event.BaseEvent;
import dn.jasm.event.EventType;
import lombok.*;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class CommentEvent extends ApplicationEvent implements BaseEvent {

    private String comment;
    private Double rating;
    private String createdAt;

    public CommentEvent(Object source,String comment,Double rating,String createdAt) {
        super(source);
        this.comment = comment;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    @Override
    public EventType getEventType() {
        return EventType.COMMENT;
    }

    @Override
    public String toString() {
        return "CommentEvent{" +
                "comment='" + comment + '\'' +
                ", rating=" + rating +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
