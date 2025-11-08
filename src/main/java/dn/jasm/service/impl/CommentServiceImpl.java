package dn.jasm.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dn.jasm.configuration.aop.Loggable;
import dn.jasm.configuration.aop.TimeResulting;
import dn.jasm.dto.comment.CommentRequest;
import dn.jasm.dto.comment.CommentResponse;
import dn.jasm.dto.comment.CommentUpdateRequest;
import dn.jasm.dto.comment.ListCommentResponse;
import dn.jasm.event.comment.CommentEvent;
import dn.jasm.event.comment.CommentUpdatedEvent;
import dn.jasm.exception.CommentNotFoundException;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.mapper.CommentMapper;
import dn.jasm.repository.CommentRepository;
import dn.jasm.repository.UserRepository;
import dn.jasm.service.RedisService;
import dn.jasm.entity.CommentEntity;
import dn.jasm.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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

    private final Map<String,ListCommentResponse> userAndComments = new HashMap<>();


    @Override
    @Transactional
    @Loggable
    public void addComment(CommentRequest commentRequest) {
        CommentEntity comment = new CommentEntity();
        comment.setComment(commentRequest.getComment());
        comment.setRating(commentRequest.getRating());
        comment.setCreatedAt(LocalDateTime.now());
        var userId = comment.getUser().getId();
        var user = userRepository.findById(userId)
                        .orElseThrow(()->new UserNotFoundException(
                                MessageFormat.format("[User with id: {0} not found]",userId)));
        List<CommentEntity> comments = user.getComments();
        comments.add(comment);
        commentRepository.save(comment);
        var commentKey = Objects.toString(comment.getId());
        redisService.writeObjectInRedis(commentKey,comment);
        publishEvent(comment);
        comment.setUser(user);
        userRepository.save(user);
        log.info("[User with username: {} add comment: {}]", user.getUsername(), commentRequest.getComment());

    }

    private void publishEvent(CommentEntity commentEntity){
        eventPublisher.publishEvent(
                new CommentEvent(this,
                        commentEntity.getComment(),
                        commentEntity.getRating(),
                        commentEntity.getCreatedAt()
                                .format(DateTimeFormatter.ofPattern("yyyy-Mm-Hh"))));
    }

    private void publishUpdatedEvent(CommentEntity commentEntity){
        eventPublisher.publishEvent(
                new CommentUpdatedEvent(this,
                        commentEntity.getId(),
                        commentEntity.getUser().getId(),
                        commentEntity.getComment()));
    }

    @Override
    @Loggable
    public CommentResponse getCommentById(Long id) {
        String cacheKey = Objects.toString(id);
        var comment = commentRepository.findById(id)
                .orElseThrow(()->new CommentNotFoundException(
                        MessageFormat.format("[Comment with id: {0} not found]",id)));
        if (redisService.checkKeyExist(cacheKey)){
            return commentMapper.mapToDto(comment);
        }
        try {
            String jsonValue = objectMapper.writeValueAsString(comment);
            redisService.writeObjectInRedis(cacheKey,jsonValue);
        }catch (JsonProcessingException e){
            log.error("[Cant put value in cache: {}]",comment.toString());
        }
        return commentMapper.mapToDto(comment);
    }

    @Override
    @Loggable
    public ListCommentResponse getCommentsByIds(List<Long> ids) {
        var comments = commentRepository.findAllById(ids);
        log.info("[Comments: {}]",comments.toString());
        var keyOfComments = comments.stream()
                .map(c->c.getId().toString())
                .collect(Collectors.toSet());
        Set<Object> commentValues = new HashSet<>(comments);
            redisService.writeObjectsInRedis(keyOfComments, commentValues);

        return commentMapper.mapToCommentResponseList(comments);
    }

    @Override
    @Loggable
    @TimeResulting
    public ListCommentResponse getCommentsWithPagination(int pageNumber, int pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        Page<CommentEntity> commentsPage = commentRepository.findAll(pageRequest);
        Set<CommentEntity> comments = new HashSet<>(commentsPage.getContent());
        var commentIds = comments.stream()
                .map(CommentEntity::getComment)
                .map(String::valueOf)
                .collect(Collectors.toSet());

        if (!commentIds.isEmpty()) {
            redisService.writeObjectsInRedis(commentIds, Collections.singleton(comments));
        }

        return commentMapper.mapToDtoSet(comments);
    }

    @Override
    @TimeResulting
    public Map<String,ListCommentResponse> getCommentsByUserId(Long userId) {
        var user = userRepository.findById(userId)
                .filter(userEntity -> userEntity.getComments() != null)
                .orElseThrow(RuntimeException::new);;
        ListCommentResponse listCommentResponse = new ListCommentResponse();
        List<CommentEntity> commentEntities = user.getComments();
        var mappingEntityListToDto = commentMapper.mapToCommentResponseList(commentEntities);
        listCommentResponse.setComments(mappingEntityListToDto.getComments());
        var username = user.getUsername();
        userAndComments.put(username,listCommentResponse);
        return userAndComments;


    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        var commentForDelete = commentMapper.mapToEntity(getCommentById(commentId));
         if (!commentRepository.existsById(commentId)){
             throw new CommentNotFoundException(
                     MessageFormat.format("[Comment with id: {0} not found]",commentId));
         }
         redisService.deleteCacheByKey(String.valueOf(commentId));
         commentRepository.deleteById(commentId);
         log.info("[Deleted comment: {}, user of comment: {}]",
                 commentForDelete.getComment(),
                 commentForDelete.getUser().getId()
         );

    }

    @Override
    @Transactional
    public void deleteComments(List<Long> commentIds) {
        if (commentIds.isEmpty()){
            throw new IllegalArgumentException("[CommentList is empty!]");
        }
        var commentsForDelete = commentRepository.findAllById(commentIds)
                        .stream()
                        .filter(Objects::nonNull)
                        .peek(commentEntity -> {
                            redisService.deleteCacheByKey(commentIds.toString());
                            commentRepository.deleteAllByIdInBatch(commentIds);
                            var deletedComment = commentEntity.getComment();
                            var ownerOfComment = commentEntity.getUser().getUsername();
                            log.info("[Deleted comment: {}, Owner: {}]",deletedComment,ownerOfComment);
                        }).collect(Collectors.toSet());
        log.info("[Deleted comments: {}]",commentsForDelete);

    }

    @Override
    @Transactional
    public void editComment(CommentUpdateRequest commentUpdateRequest,Long userId) {
        var commentId = commentUpdateRequest.getCommentId();
        var user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        commentRepository.findById(commentId)
                .stream()
                .peek(commentEntity -> {
                    commentEntity.setComment(commentUpdateRequest.getCommentContent());
                    commentEntity.setUpdatedAt(LocalDateTime.now());
                    commentEntity.setUser(user);
                    commentRepository.save(commentEntity);
                    userRepository.save(user);
                    publishUpdatedEvent(commentEntity);
                    log.info("[Edited comment: {}, User which update comment: {}]",commentEntity.getComment(),userId);
                })
                .map(commentMapper::mapToDto)
                .forEach(updatedComment->log.info("[Comment {} is updated!]",commentUpdateRequest));
    }

    @Override
    public void addSubComment(Long commentId, String content) {

    }

    @Override
    @EventListener
    @Loggable
    public void handleCommentCreateEvent(CommentEvent commentEvent) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            String message = objectMapper.writeValueAsString(commentEvent);
            CompletableFuture<Void> redisFuture = CompletableFuture.runAsync(() ->
                    redisService.writeObjectInRedis(commentEvent.getComment(), message));
            CompletableFuture.allOf(redisFuture)
                    .exceptionally(throwable -> {
                        log.error("[Error processing comment event: {}]", throwable.getMessage());
                        return null;
                    });
        } catch (JsonProcessingException e) {
            log.error("[Can't serialize comment message: {}]", e.getMessage());
        }
    }

    @Override
    @EventListener
    public void handleCommentUpdateEvent(CommentUpdatedEvent commentUpdatedEvent) {
        redisService.writeObjectInRedis(
                String.valueOf(commentUpdatedEvent.getCommentId()),
                commentUpdatedEvent.getNewComment()
        );
        log.info("[Handle Updating of comment: id: {}, new comment: {}]",
                commentUpdatedEvent.getCommentId(),
                commentUpdatedEvent.getNewComment()
        );
    }
}
