package dn.jasm.service;

import dn.jasm.dto.comment.CommentRequest;
import dn.jasm.dto.comment.CommentResponse;
import dn.jasm.dto.comment.ListCommentResponse;

import java.util.List;
import java.util.Map;

public interface CommentService {

    void addComment(CommentRequest commentRequest, Long userId);

    CommentResponse getCommentById(Long id);

    List<CommentResponse> getCommentsByIds(List<Long> ids);

    ListCommentResponse getCommentsWithPagination(int pageNumber, int pageSize);

    Map<String,List<CommentResponse>> getCommentsByUserId(Long userId);

    void deleteComment(Long commentId);

    void deleteComments(List<Long> commentIds);

    void editComment(Long commentId,Long userId);

    void addSubComment(Long commentId, String content);
}
