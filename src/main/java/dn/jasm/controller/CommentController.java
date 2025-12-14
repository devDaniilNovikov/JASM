package dn.jasm.controller;

import dn.jasm.configuration.swagger.comment.SwaggerAnnotationForComment;
import dn.jasm.configuration.swagger.comment.SwaggerAnnotationForCommentCollection;
import dn.jasm.dto.comment.*;
import dn.jasm.service.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Comment" ,description = "Действия с комментариями")
public class CommentController {

    private static final String ADD_COMMENT = "/api/v1/comments/add";
    private static final String GET_COMMENTS_BY_USER = "/api/v1/comments/by-userId";
    private static final String GET_COMMENT_BY_ID = "/api/v1/comment/{id}";
    private static final String GET_COMMENTS_BY_IDS = "/api/v1/comments";
    private static final String GET_COMMENTS_WITH_PAGINATION = "/api/v1/comments/list";
    private static final String DELETE_COMMENT = "/api/v1/comment/delete/{id}";
    private static final String DELETE_MULTIPLE_COMMENTS = "/api/v1/comments/delete";
    private static final String EDIT_COMMENT = "/api/v1/comment/edit";
    private static final String PAGE_SIZE_DEFAULT_VALUE = "10";
    private static final String PAGE_NUMBER_DEFAULT_VALUE = "0";

    private final CommentService commentService;

    @DeleteMapping(DELETE_MULTIPLE_COMMENTS)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SwaggerAnnotationForComment(operation = "Удаление нескольких комментариев")
    public void deleteComments(List<Long> ids){
        commentService.deleteComments(ids);
    }

    @PatchMapping(EDIT_COMMENT)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForComment(operation = "Редактирование комментария")
    public void editComment(@RequestBody CommentUpdateRequest commentUpdateRequest,
                            @RequestParam Long userId){
        commentService.editComment(commentUpdateRequest,userId);
    }


    @PostMapping(ADD_COMMENT)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForComment(operation = "Добавление комментария")
    public void addComment(@RequestBody CommentRequest commentRequest) {
        commentService.addComment(commentRequest);
    }

    @GetMapping(GET_COMMENTS_BY_USER)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForCommentCollection(operation = "Получение списка комментариев пользователя")
    public MapCommentResponse getCommentsOfUserByUserId(@RequestParam Long userId){
        return commentService.getCommentsByUserId(userId);
    }

    @GetMapping(GET_COMMENT_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForComment(operation = "Получение комментария")
    public CommentResponse commentResponse(@PathVariable Long id){
        return commentService.getCommentById(id);
    }

    @GetMapping(GET_COMMENTS_BY_IDS)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForCommentCollection(operation = "Получение списка комментариев по их уникальным идентификаторам")
    public ListCommentResponse getCommentByIds(@RequestParam List<Long> ids){
        return commentService.getCommentsByIds(ids);
    }

    @GetMapping(GET_COMMENTS_WITH_PAGINATION)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForCommentCollection(operation = "Получение списка комментариев с пагинацией")
    public ListCommentResponse getCommentsWithPagination(@RequestParam(defaultValue = PAGE_NUMBER_DEFAULT_VALUE) int pageNumber,
                                                         @RequestParam(defaultValue = PAGE_SIZE_DEFAULT_VALUE)
                                                         @Min(1) int pageSize){
        return commentService.getCommentsWithPagination(pageNumber,pageSize);
    }

    @DeleteMapping(DELETE_COMMENT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SwaggerAnnotationForComment(operation = "Удаление комментария")
    public void deleteComment(@PathVariable Long id){
        commentService.deleteComment(id);
    }
}
