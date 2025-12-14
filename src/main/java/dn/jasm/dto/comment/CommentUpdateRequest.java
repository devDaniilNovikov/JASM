package dn.jasm.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CommentResponse", description = "ДТО для редактирования комментария")
public class CommentUpdateRequest {

    @NotNull(message = "CommentId can't be null")
    @Schema(name = "commentId", description = "Уникальный идентификатор комментария")
    private Long commentId;

    @Schema(name = "text", description = "Текст для обновления комментария")
    @NotBlank(message = "Text of comment can't be blank")
    private String text;
}
