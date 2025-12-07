package dn.jasm.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class MapCommentResponse {

    @JsonProperty(value = "comments_of_user")
    private Map<String,ListCommentResponse> commentMap = new HashMap<>();

}
