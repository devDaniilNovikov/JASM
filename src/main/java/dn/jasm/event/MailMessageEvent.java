package dn.jasm.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;


@Getter
@Setter
public class MailMessageEvent extends ApplicationEvent implements BaseEvent {

    private String from;
    private String content;
    private LocalDateTime createdAt;
    private String to;

    public MailMessageEvent(Object source,
                            String content,
                            LocalDateTime createdAt,
                            String to,
                            String from) {
        super(source);
        this.content = content;
        this.createdAt = createdAt;
        this.to = to;
        this.from = from;
    }

    @Override
    public String toString() {
        return "MailMessageEvent{" +
                "content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", to='" + to + '\'' +
                '}';
    }

    @Override
    public EventType getEventType() {
        return EventType.MAIL_MESSAGE;
    }
}
