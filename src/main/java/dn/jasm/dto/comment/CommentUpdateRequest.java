package dn.jasm.dto.comment;

import lombok.Data;

@Data
public class CommentUpdateRequest {

   private Long commentId;
   private String commentContent;
}
