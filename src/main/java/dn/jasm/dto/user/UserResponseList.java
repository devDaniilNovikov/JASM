package dn.jasm.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "UserResponseList", description = "Список пользователей")
public class UserResponseList {

    @Schema(name = "users", description = "Список запрашиваемых пользователей")
    private List<UserResponse> users = new ArrayList<>();
}
