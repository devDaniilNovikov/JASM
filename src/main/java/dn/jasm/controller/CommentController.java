package dn.jasm.controller;

import dn.jasm.dto.comment.CommentRequest;
import dn.jasm.dto.comment.CommentResponse;
import dn.jasm.dto.comment.CommentUpdateRequest;
import dn.jasm.service.CommentService;
import dn.jasm.dto.comment.ListCommentResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private static final String ADD_COMMENT = "/api/v1/comments/add";
    private static final String GET_COMMENTS_BY_USER = "/api/v1/comments/by-userId";
    private static final String GET_COMMENT_BY_ID = "/api/v1/comment/{id}";
    private static final String GET_COMMENTS_BY_IDS = "/api/v1/comments";
    private static final String GET_COMMENTS_WITH_PAGINATION = "/api/v1/comments/list";
    private static final String DELETE_COMMENT = "/api/v1/comment/delete/{id}";
    private static final String DELETE_MULTIPLE_COMMENTS = "/api/v1/comments/delete";
    private static final String EDIT_COMMENT = "/api/v1/comment/edit";

    private final CommentService commentService;

    @DeleteMapping(DELETE_MULTIPLE_COMMENTS)
    public void deleteComments(List<Long> ids){
        commentService.deleteComments(ids);
    }

    @PatchMapping(EDIT_COMMENT)
    public void editComment(@RequestBody CommentUpdateRequest commentUpdateRequest,
                            @RequestParam Long userId){
        commentService.editComment(commentUpdateRequest,userId);
    }


    @PostMapping(ADD_COMMENT)
    @ResponseStatus(HttpStatus.OK)
    public void addComment(@RequestBody CommentRequest commentRequest) {

        commentService.addComment(commentRequest);
    }

    @GetMapping(GET_COMMENTS_BY_USER)
    public Map<String,ListCommentResponse> getCommentsOfUserByUserId(@RequestParam Long userId){
        return commentService.getCommentsByUserId(userId);
    }

    @GetMapping(GET_COMMENT_BY_ID)
    public CommentResponse commentResponse(@PathVariable Long id){
        return commentService.getCommentById(id);
    }

    @GetMapping(GET_COMMENTS_BY_IDS)
    public ListCommentResponse getCommentByIds(@RequestParam List<Long> ids){
        return commentService.getCommentsByIds(ids);
    }

    @GetMapping(GET_COMMENTS_WITH_PAGINATION)
    public ListCommentResponse getCommentsWithPagination(@RequestParam(defaultValue = "0") int pageNumber,
                                                         @RequestParam(defaultValue = "10")
                                                         @Min(1) int pageSize){
        return commentService.getCommentsWithPagination(pageNumber,pageSize);
    }

    @DeleteMapping(DELETE_COMMENT)
    public void deleteComment(@PathVariable Long id){
        commentService.deleteComment(id);
    }
}
