package dn.jasm.dto.comment;

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


    @Size(min = 2,max = 100,message = "Text can't be null")
    private String comment;
    @Size(min = 1,max = 5)
    private Double rating;
    private Long itemId;
    private Long userId;


}
