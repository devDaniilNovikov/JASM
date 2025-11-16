package dn.jasm.async;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Table(schema = "jasm", name = "tasks")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AsyncTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

}
