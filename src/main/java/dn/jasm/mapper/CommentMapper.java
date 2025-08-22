package dn.jasm.mapper;

import dn.jasm.dto.comment.CommentRequest;
import dn.jasm.dto.comment.CommentResponse;
import dn.jasm.dto.comment.ListCommentResponse;
import dn.jasm.entity.CommentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper extends Mappable<CommentEntity, CommentResponse> {


    @Mapping(source = "rating", target = "rating")
    @Mapping(source = "comment", target = "comment")
    @Mapping(target = "ownerName",source = "user.username")
    @Override
    CommentResponse toDto(CommentEntity entity);


    CommentResponse toResponse(CommentRequest commentRequest);

    default ListCommentResponse toList(List<CommentEntity> comments){
        ListCommentResponse listCommentResponse = new ListCommentResponse();
        listCommentResponse.setComments(comments.stream()
                .map(this::toDto)
                .toList());
        return listCommentResponse;
    }


}
