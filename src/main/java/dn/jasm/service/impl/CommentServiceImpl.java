package dn.jasm.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dn.jasm.configuration.aop.Loggable;
import dn.jasm.configuration.aop.TimeResulting;
import dn.jasm.configuration.redis.CacheNames;
import dn.jasm.dto.comment.*;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {


    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final RedisTemplate<String,Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectsMapper;
    private static final long CACHE_TTL = 10;



    @Override
    @Transactional
    @Loggable
    public void addComment(CommentRequest commentRequest) {
        CommentEntity comment = new CommentEntity();
        comment.setComment(commentRequest.getComment());
        comment.setRating(commentRequest.getRating());
        comment.setCreatedAt(LocalDateTime.now());
        var userId = commentRequest.getUserId();
        var user = userRepository.findById(userId)
                        .orElseThrow(()->new UserNotFoundException(
                                MessageFormat.format("[User with id: {0} not found]",userId)));
        commentRepository.save(comment);
        List<CommentEntity> comments = user.getComments();
        if (comments!=null){
            comments = new ArrayList<>();
            user.setComments(comments);
        }
        comment.setUser(user);
        userRepository.save(user);
        var cacheKey = CacheNames.COMMENT_CACHE
                        .getValue()
                        .concat(comment.getId().toString());
        redisTemplate.opsForValue()
                .set(cacheKey,
                     comment,
                     CACHE_TTL,
                     TimeUnit.MINUTES);
        publishEvent(comment);
        log.info("[User with username: {} add comment: {}]",
                user.getUsername(),
                commentRequest.getComment());

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
        String cacheKey = CacheNames.ITEM_CACHE
                .getValue()
                .concat(id.toString());
        var cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (cacheValue!=null){
            cacheLogging(cacheValue);
            return objectsMapper.convertValue(cacheValue, CommentResponse.class);
        }
        return commentRepository.findById(id)
                .stream()
                .map(commentMapper::mapToDto)
                .peek(commentResponse -> {
                    redisTemplate.opsForValue()
                            .set(cacheKey,
                                 commentResponse,
                                 CACHE_TTL,
                                 TimeUnit.MINUTES);
                    dataBaseLogging(commentResponse);
                })
                .findFirst()
                .orElseThrow(CommentNotFoundException::new);
    }

    @Override
    @Loggable
    public ListCommentResponse getCommentsByIds(List<Long> ids) {
        var comments = commentRepository.findAllById(ids);
        log.info("[Comments: {}]",comments );
        var cacheKeysOfComments = comments.stream()
                .map(CommentEntity::getId)
                .map(String::valueOf)
                .map(c->CacheNames.COMMENT_CACHE
                        .getValue()
                        .concat(c))
                .toList();
        var cacheValues = redisTemplate.opsForValue().multiGet(cacheKeysOfComments);
        if (cacheValues!=null && cacheValues.stream().anyMatch(Objects::nonNull)){
            List<CommentResponse> commentList = cacheValues.stream()
                    .map(comment->objectsMapper.convertValue(comment, CommentResponse.class))
                    .toList();
            ListCommentResponse listCommentResponse = new ListCommentResponse();
            listCommentResponse.setComments(commentList);
            cacheLogging(cacheValues);
            return listCommentResponse;
        }
        dataBaseLogging(comments);
        return commentMapper.mapToCommentResponseList(comments);
    }

    @Override
    @Loggable
    @TimeResulting
    public ListCommentResponse getCommentsWithPagination(int pageNumber, int pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        var comments = commentRepository.findAll(pageRequest)
                .stream()
                .toList();
        var keys = comments.stream()
                .map(CommentEntity::getId)
                .map(String::valueOf)
                .map(c->CacheNames.COMMENT_CACHE
                        .getValue()
                        .concat(c))
                .toList();
        var cacheValues = redisTemplate.opsForValue().multiGet(keys);
        log.info("Cache values of comments: {}",cacheValues);
        if (cacheValues!=null && cacheValues.stream()
                .allMatch(Objects::nonNull)) {
            List<CommentResponse> commentList = cacheValues.stream()
                    .map(c -> objectsMapper.convertValue(c, CommentResponse.class))
                    .toList();
            ListCommentResponse listCommentResponse = new ListCommentResponse();
            listCommentResponse.setComments(commentList);
            cacheLogging(cacheValues);
            return listCommentResponse;
        }
        comments.forEach(c->{
            redisTemplate.opsForValue()
                    .set(c.getId().toString(),c,CACHE_TTL,TimeUnit.MINUTES);
        });
        dataBaseLogging(comments);
        return commentMapper.mapToDtoList(comments);
    }

    @Override
    @TimeResulting
    public MapCommentResponse getCommentsByUserId(Long userId) {
        var user = userRepository.findById(userId)
                .filter(userEntity -> userEntity.getComments() != null)
                .orElseThrow(RuntimeException::new);;
        var commentsIds = commentRepository.findAllByUserId(userId)
                .stream()
                .map(CommentEntity::getId)
                .map(String::valueOf)
                .toList();
        var commentCacheValue = redisTemplate.opsForValue().multiGet(commentsIds);
        if (commentCacheValue!=null && commentCacheValue.stream()
                .allMatch(Objects::nonNull)){
            List<CommentResponse> comments = commentCacheValue.stream()
                    .map(comment->objectsMapper.convertValue(comment, CommentResponse.class))
                    .toList();
            ListCommentResponse listCommentResponse = new ListCommentResponse();
            listCommentResponse.setComments(comments);
            MapCommentResponse commentResponseMap = new MapCommentResponse();
            commentResponseMap.setCommentMap(Map.of(user.getUsername(), listCommentResponse));
            cacheLogging(commentCacheValue);
            return commentResponseMap;

        }
        var idsLongValues = commentsIds.stream()
                        .map(Long::valueOf)
                        .toList();
        var comments = commentRepository.findAllById(idsLongValues);
        var commentsDto = commentMapper.mapToCommentResponseList(comments);
        MapCommentResponse commentResponseMap = new MapCommentResponse();
        commentResponseMap.setCommentMap(Map.of(user.getUsername(),commentsDto));
        comments.forEach(comment->redisTemplate.opsForValue()
                .set(comment.getId().toString(), comment, CACHE_TTL, TimeUnit.MINUTES));
        dataBaseLogging(comments);
        return commentResponseMap;


    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        var commentForDelete = commentMapper.mapToEntity(getCommentById(commentId));
         if (!commentRepository.existsById(commentId)){
             throw new CommentNotFoundException(
                     MessageFormat.format("[Comment with id: {0} not found]",commentId));
         }
         CompletableFuture.runAsync(()->redisTemplate.delete(commentId.toString()))
                         .thenRunAsync(()->commentRepository.deleteById(commentId))
                         .exceptionally(r->{
                             if (r!=null){
                                 log.error("Error in async task cause: {}",r.getMessage());
                             }
                             return null;
                         });
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
                            redisTemplate.delete(commentIds.toString());
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
                    commentEntity.setComment(commentUpdateRequest.getText());
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
            CompletableFuture<Void> redisFuture = CompletableFuture.runAsync(() ->
                    redisTemplate.opsForValue()
                            .set(commentEvent.getId(),
                                    commentEvent.getComment(),
                                    CACHE_TTL,TimeUnit.MINUTES));
            CompletableFuture.allOf(redisFuture)
                    .exceptionally(throwable -> {
                        log.error("[Error processing comment event: {}]", throwable.getMessage());
                        return null;
                    });
    }

    @Override
    @EventListener
    public void handleCommentUpdateEvent(CommentUpdatedEvent commentUpdatedEvent) {
        redisTemplate.opsForValue()
                        .set(String.valueOf(
                                commentUpdatedEvent.getCommentId()),
                                commentUpdatedEvent.getNewComment(),
                                CACHE_TTL,
                                TimeUnit.MINUTES
        );
        log.info("[Handle Updating of comment: id: {}, new comment: {}]",
                commentUpdatedEvent.getCommentId(),
                commentUpdatedEvent.getNewComment()
        );
    }

    private static void cacheLogging(Object element) {
        log.info("[Value: {} will get from cache]",element);
    }

    private static void dataBaseLogging(Object element) {
        log.info("[Value: {} will get from db]",element);
    }
}
