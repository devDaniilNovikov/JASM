package dn.jasm.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.text.MessageFormat;


public record UserSessionLoginDto(@NotNull(message = "userId can't be null")
                                  Long userId,
                                  @NotNull(message = "username can't be null")
                                  @Size(min = 5,max = 64, message = "username must be not min 5, and dont be max 64")
                                  String username)
{}
