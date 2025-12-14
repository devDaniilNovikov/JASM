package dn.jasm.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.text.MessageFormat;

@Schema(name = "UserSessionLogin", description = "Получение пользовательской сессии")
public record UserSessionLoginDto(@NotNull(message = "userId can't be null")
                                  @Schema(name = "userId", description = "Уникальный идентификатор пользователя")
                                  Long userId,

                                  @Schema(name = "username", description = "Никнейм пользователя")
                                  @NotNull(message = "username can't be null")
                                  @Size(min = 5,max = 64, message = "username must be not min 5, and dont be max 64")
                                  String username)
{}
