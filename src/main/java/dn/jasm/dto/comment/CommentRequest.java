package dn.jasm.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "CommentRequest", description = "ДТО для создания комментария")
public class CommentRequest {


    @Size(min = 2,max = 100,message = "Size of text can have symbols quantity between 2 and 100")
    @NotBlank(message = "Text of comment can't be blank")
    @Schema(name = "comment", description = "Комментарий")
    private String comment;

    @Size(min = 1,max = 5)
    @Schema(name = "rating", description = "Оценка")
    private Double rating;

    @Schema(name = "itemId", description = "Уникальный идентификатор предмета, к которому оставляется комментарий")
    @NotNull(message = "ItemId can't be null")
    private Long itemId;

    @Schema(name = "userId", description = "Уникальный идентификатор пользователя, который оставляет комментарий")
    @NotNull(message = "UserId can't be null")
    private Long userId;


}
