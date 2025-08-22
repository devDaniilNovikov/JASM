package dn.jasm.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dn.jasm.configuration.aop.Loggable;
import dn.jasm.configuration.aop.TimeResulting;
import dn.jasm.dto.comment.CommentRequest;
import dn.jasm.dto.comment.CommentResponse;
import dn.jasm.dto.comment.ListCommentResponse;
import dn.jasm.event.CommentEvent;
import dn.jasm.exception.CommentNotFoundException;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.mapper.CommentMapper;
import dn.jasm.repository.CommentRepository;
import dn.jasm.repository.UserRepository;
import dn.jasm.configuration.redis.RedisService;
import dn.jasm.entity.CommentEntity;
import dn.jasm.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {


    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final RedisService redisService;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;



    public String mapObjectToString(Object object){
        return String.valueOf(object).trim();
    }


    @Override
    @Transactional
    @Loggable
    public void addComment(CommentRequest commentRequest, Long userId) {
        CommentEntity comment = new CommentEntity();
        comment.setComment(commentRequest.getComment());
        comment.setRating(commentRequest.getRating());
        comment.setCreatedAt(LocalDateTime.now());
        var user = userRepository.findById(userId)
                        .orElseThrow(()->new UserNotFoundException(
                                MessageFormat.format("User with id: {0} not found",userId)));
        List<CommentEntity> comments = user.getComments();
        comments.add(comment);
        commentRepository.save(comment);
        var commentKey = mapObjectToString(comment.getRating());
        redisService.writeObjectInRedis(commentKey,comment);
        eventPublisher.publishEvent(new CommentEvent(this, commentRequest.getComment(),
                        commentRequest.getRating(),LocalDateTime.now())
        );
        comment.setUser(user);
        userRepository.save(user);
        log.info("User with username: {} add comment: {}", user.getUsername(), commentRequest.getComment());


    }

    @Override
    @Loggable
    public CommentResponse getCommentById(Long id) {
        String cacheKey = mapObjectToString(id);
        var comment = commentRepository.findById(id)
                .orElseThrow(()->new CommentNotFoundException(
                        MessageFormat.format("Comment with id: {0} not found",id)));
        if (redisService.checkKeyExist(cacheKey)){
            return commentMapper.toDto(comment);
        }
        try {
            var jsonString = objectMapper.writeValueAsString(comment);
            redisService.writeObjectInRedis(cacheKey,jsonString);
        }catch (JsonProcessingException e){
            log.error("Cant put value in cache: {}",comment.toString());
        }
        return commentMapper.toDto(comment);
    }

    @Override
    @Loggable
    public List<CommentResponse> getCommentsByIds(List<Long> ids) {
        var comments = commentRepository.findAllById(ids);
        log.info("Comments: {}",comments.toString());
        var keyOfComments = comments.stream()
                .map(c->c.getId().toString())
                .toList();
        List<Object> commentValues = Collections.singletonList(comments);
            redisService.writeObjectsInRedis(keyOfComments, commentValues);

        return commentMapper.toDtoList(comments);
    }

    @Override
    @Loggable
    @TimeResulting
    public ListCommentResponse getCommentsWithPagination(int pageNumber, int pageSize) {
        var pageRequest = PageRequest.of(pageNumber, pageSize);
        var commentsPage = commentRepository.findAll(pageRequest);

        var comments = commentsPage.getContent();
        var commentIds = comments.stream()
                .map(comment -> comment.getId().toString())
                .toList();

        if (!commentIds.isEmpty()) {
            redisService.writeObjectsInRedis(commentIds, Collections.singletonList(comments));
        }

        return commentMapper.toList(comments);
    }

    @Override
    @TimeResulting
    public Map<String,List<CommentResponse>> getCommentsByUserId(Long userId) {
        var user = userRepository.findById(userId)
                .filter(userEntity -> userEntity.getComments() != null)
                .orElseThrow(RuntimeException::new);
        Map<String,List<CommentEntity>> userAndComments = new HashMap<>();
        List<CommentResponse> commentRequests = user.getComments()
                .stream()
                .map(comment->commentMapper.toResponse(CommentRequest.builder()
                        .comment(comment.getComment())
                        .rating(comment.getRating())
                        .build()))
                .toList();
        String username = user.getUsername().toUpperCase().trim();
        userAndComments.put(username,commentMapper.toEntityList(commentRequests));
        Map<String,List<CommentResponse>> mappingMap = userAndComments
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry->commentMapper.toDtoList(entry.getValue())));
        log.info("Comment map: {}",mappingMap.toString());
        return mappingMap;

    }

    @Override
    public void deleteComment(Long commentId) {
        var commentForDelete = commentMapper.toEntity(getCommentById(commentId));
         if (!commentRepository.existsById(commentId)){
             throw new CommentNotFoundException(
                     MessageFormat.format("Comment with id: {0} not found",commentId));
         }
         redisService.deleteCacheByKey(String.valueOf(commentId));
         commentRepository.deleteById(commentId);
         log.info("Deleted comment: {}, user of comment: {}",
                 commentForDelete.getComment(),
                 commentForDelete.getUser().getId()
         );

    }

    @Override
    public void deleteComments(List<Long> commentIds) {
        if (commentIds.isEmpty()){
            throw new IllegalArgumentException("CommentList is empty!");
        }
        var commentsForDelete = commentRepository.findAllById(commentIds)
                        .stream()
                        .filter(Objects::nonNull)
                        .peek(commentEntity -> {
                            redisService.deleteCacheByKey(commentIds.toString());
                            commentRepository.deleteAllByIdInBatch(commentIds);
                            var deletedComment = commentEntity.getComment();
                            var ownerOfComment = commentEntity.getUser().getUsername();
                            log.info("Deleted comment: {}, Owner: {}",deletedComment,ownerOfComment);
                        }).collect(Collectors.toSet());
        log.info("Deleted comments: {}",commentsForDelete.toString());

    }

    @Override
    public void editComment(Long commentId, Long userId) {

    }

    @Override
    public void addSubComment(Long commentId, String content) {

    }
}
