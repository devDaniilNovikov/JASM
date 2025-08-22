package dn.jasm.event;


import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;


@Getter
@Setter
public class UserCreateEvent extends ApplicationEvent {

    private String username;
    private String phoneNumber;
    private LocalDateTime createdAt;

    @Override
    public String toString() {
        return "UserCreateEvent{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

    public UserCreateEvent(Object source, String username, String phoneNumber, LocalDateTime createdAt) {
        super(source);
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
    }
}
