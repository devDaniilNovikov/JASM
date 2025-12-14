package dn.jasm.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Schema(name = "SessionKeys", description = "Уникальные идентификаторы пользовательских сессий")
public record SessionKeysRequest(
        @Schema(name = "keys",description = "Уникальные идентификаторы")
        Set<String> keys
) {

}
