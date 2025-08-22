package dn.jasm.dto.comment;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ListCommentResponse {

    private List<CommentResponse> comments = new ArrayList<>();
}
