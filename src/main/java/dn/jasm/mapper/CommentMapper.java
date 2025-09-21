package dn.jasm.mapper;


import dn.jasm.dto.comment.CommentResponse;
import dn.jasm.dto.comment.ListCommentResponse;
import dn.jasm.entity.CommentEntity;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    private final UserRepository userRepository;

    public CommentResponse mapToDto(CommentEntity commentEntity){
        return CommentResponse.builder()
                .comment(commentEntity.getComment())
                .rating(commentEntity.getRating())
                .ownerName(commentEntity.getUser().getUsername())
                .createdAt(commentEntity.getCreatedAt())
                .build();
    }

    public CommentEntity mapToEntity(CommentResponse commentResponse){
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setComment(commentResponse.getComment());
        commentEntity.setRating(commentEntity.getRating());
        var user = userRepository.findByUsername(commentResponse.getOwnerName())
                        .orElseThrow(()->new UserNotFoundException(
                                MessageFormat.format(
                                        "User with id: {0} not found",commentResponse.getOwnerName())
                        ));
        commentEntity.setUser(user);
        commentEntity.setCreatedAt(LocalDateTime.now());
        commentEntity.setUpdatedAt(LocalDateTime.now());
        return commentEntity;
    }

    public ListCommentResponse mapToDtoSet(Set<CommentEntity> comments){
        ListCommentResponse listCommentResponse = new ListCommentResponse();
        listCommentResponse.setComments(comments.stream().map(this::mapToDto).toList());
        return listCommentResponse;
    }

    public List<CommentEntity> mapToEntityList(List<CommentResponse> commentResponses){
        return commentResponses.stream().map(this::mapToEntity).toList();
    }

    public ListCommentResponse mapToCommentResponseList(List<CommentEntity> comments){
        ListCommentResponse listCommentResponse = new ListCommentResponse();
        listCommentResponse.setComments(comments.stream().map(this::mapToDto).toList());
        return listCommentResponse;
    }


}
