package dn.jasm.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(name = "ListComment", description = "Список комментариев")
public class ListCommentResponse {

    @JsonProperty(namespace = "list_of_comments")
    @Schema(name = "comments", description = "Список запрашиваемых комментариев")
    private List<CommentResponse> comments = new ArrayList<>();
}
