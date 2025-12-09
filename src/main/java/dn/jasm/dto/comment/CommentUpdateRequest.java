package dn.jasm.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentUpdateRequest {

    @NotNull(message = "CommentId can't be null")
    private Long commentId;
    @NotBlank(message = "Text of comment can't be blank")
    private String text;
}
