package dn.jasm.dto.comment;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "CommentResponse", description = "Информация о созданном комментарии")
public class CommentResponse {

    @Schema(name = "id", description = "Уникальный идентификатор комментария")
    private Long id;

    @Schema(name = "comment", description = "Комментарий")
    private String comment;

    @Schema(name = "rating", description = "Оценка , оставленная с комментарием")
    private Double rating;

    @Schema(name = "updatedAt", description = "Время обновления комментария")
    private String updatedAt;

    @Schema(name = "userId", description = "Время создания комментария")
    private String createdAt;

    @Schema(name = "userId", description = "Никнейм пользователя, который оставил комментарий")
    @JsonProperty("owner_name")
    private String ownerName;

}
