package dn.jasm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(schema = "jasm",name = "notifications")
@Getter
@Setter
public class NotificationEntity extends BasedEntity {

    private String message;

    @DateTimeFormat(pattern = "yyyy-MM-dd",iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAt;

    private String recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
