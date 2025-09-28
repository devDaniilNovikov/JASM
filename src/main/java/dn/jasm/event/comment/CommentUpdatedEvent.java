package dn.jasm.event.comment;

import dn.jasm.event.BaseEvent;
import dn.jasm.event.EventType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class CommentUpdatedEvent extends ApplicationEvent implements BaseEvent {

    private Long commentId;
    private Long ownerId;
    private String newComment;

    public CommentUpdatedEvent(Object source,
                               Long commentId,
                               Long ownerId,
                               String newComment) {
        super(source);
        this.commentId = commentId;
        this.ownerId = ownerId;
        this.newComment = newComment;
    }

    @Override
    public EventType getEventType() {
        return EventType.COMMENT_UPDATED;
    }
}
