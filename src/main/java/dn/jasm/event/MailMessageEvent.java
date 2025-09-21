package dn.jasm.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;


@Getter
@Setter
public class MailMessageEvent extends ApplicationEvent {

    private String content;
    private LocalDateTime createdAt;
    private String to;

    public MailMessageEvent(Object source,
                            String content,
                            LocalDateTime createdAt,
                            String to) {
        super(source);
        this.content = content;
        this.createdAt = createdAt;
        this.to = to;
    }

    @Override
    public String toString() {
        return "MailMessageEvent{" +
                "content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", to='" + to + '\'' +
                '}';
    }
}
