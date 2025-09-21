package dn.jasm.event.user;

import lombok.*;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserUpdateEvent extends ApplicationEvent {
    
    private String username;
    private String phoneNumber;
    private LocalDateTime updatedAt;
    private Boolean isUpdate;

    public UserUpdateEvent(Object source,
                           String username,
                           String phoneNumber,
                           LocalDateTime updatedAt,
                           Boolean isUpdate) {
        super(source);
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.updatedAt = updatedAt;
        this.isUpdate = isUpdate;
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

}
