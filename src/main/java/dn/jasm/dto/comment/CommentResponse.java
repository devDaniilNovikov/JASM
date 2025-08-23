package dn.jasm.dto.comment;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {

    private String comment;
    private Double rating;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("created_At")
    private LocalDateTime createdAt;

    @JsonProperty("owner_name")
    private String ownerName;

}
