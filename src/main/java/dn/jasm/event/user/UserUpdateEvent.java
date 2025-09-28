package dn.jasm.event.user;

import dn.jasm.event.BaseEvent;
import dn.jasm.event.EventType;
import lombok.*;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
@Setter
public class UserUpdateEvent extends ApplicationEvent implements BaseEvent {
    
    private String username;
    private String phoneNumber;
    private LocalDateTime updatedAt;
    private String timeOfUpdating;
    private Boolean isUpdate;
    private String eventId = UUID.randomUUID().toString();

    public UserUpdateEvent(Object source,
                           String username,
                           String phoneNumber,
                           LocalDateTime updatedAt,
                           Boolean isUpdate,
                           String eventId) {
        super(source);
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.updatedAt = updatedAt;
        this.isUpdate = isUpdate;
        this.eventId = eventId;
    }

    public UserUpdateEvent(Object source,
                           String username,
                           String phoneNumber,
                           LocalDateTime updatedAt,
                           String eventId) {
        super(source);
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.timeOfUpdating = updatedAt.format(DateTimeFormatter.ofPattern("yyyy-Mm-Dd: Hh:mm:ss"));
        this.eventId = eventId;
    }


    @Override
    public String toString() {
        return "UserUpdateEvent{" +
                "username='" + username + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", updatedAt=" + updatedAt +
                ", isUpdate=" + isUpdate +
                '}';
    }

    @Override
    public EventType getEventType() {
        return EventType.USER_UPDATE;
    }
}
