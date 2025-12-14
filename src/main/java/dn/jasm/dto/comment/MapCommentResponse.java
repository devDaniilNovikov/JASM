package dn.jasm.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MapComment", description = "Список комментариев пользователя")
public class MapCommentResponse {

    @JsonProperty(value = "comments_of_user")
    @Schema(name = "commentMap", description = "Список комментариев пользователя по его никнейму")
    private Map<String,ListCommentResponse> commentMap = new HashMap<>();

}
