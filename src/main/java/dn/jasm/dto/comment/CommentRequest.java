package dn.jasm.dto.comment;

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
public class CommentRequest {


    @Size(min = 2,max = 100,message = "Size of text can have symbols quantity between 2 and 100")
    @NotBlank(message = "Text of comment can't be blank")
    private String comment;
    @Size(min = 1,max = 5)
    private Double rating;
    @NotNull(message = "ItemId can't be null")
    private Long itemId;
    @NotNull(message = "UserId can't be null")
    private Long userId;


}
