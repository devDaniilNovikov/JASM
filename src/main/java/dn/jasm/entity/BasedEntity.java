package dn.jasm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BasedEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -123948062323780121L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime updatedAt;

    public BasedEntity(LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
    }
}
