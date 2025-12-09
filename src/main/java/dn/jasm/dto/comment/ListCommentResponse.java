package dn.jasm.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ListCommentResponse {

    @JsonProperty(namespace = "list_of_comments")
    private List<CommentResponse> comments = new ArrayList<>();
}
