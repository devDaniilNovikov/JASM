package dn.jasm.service;

import dn.jasm.dto.comment.CommentRequest;
import dn.jasm.dto.comment.CommentResponse;
import dn.jasm.dto.comment.CommentUpdateRequest;
import dn.jasm.dto.comment.ListCommentResponse;
import dn.jasm.event.comment.CommentEvent;
import dn.jasm.event.comment.CommentUpdatedEvent;

import java.util.List;
import java.util.Map;

public interface CommentService {

    void addComment(CommentRequest commentRequest);

    CommentResponse getCommentById(Long id);

    ListCommentResponse getCommentsByIds(List<Long> ids);

    ListCommentResponse getCommentsWithPagination(int pageNumber, int pageSize);

    Map<String,ListCommentResponse> getCommentsByUserId(Long userId);

    void deleteComment(Long commentId);

    void deleteComments(List<Long> commentIds);

    void editComment(CommentUpdateRequest commentUpdateRequest,Long userId);

    void addSubComment(Long commentId, String content);

    void handleCommentCreateEvent(CommentEvent commentEvent);

    void handleCommentUpdateEvent(CommentUpdatedEvent commentUpdatedEvent);
}
