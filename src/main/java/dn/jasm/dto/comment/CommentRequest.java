package dn.jasm.dto.comment;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentRequest {


    @Size(min = 2,max = 100,message = "Text can't be null")
    private String comment;
    @Size(min = 1,max = 5)
    private Double rating;


}
