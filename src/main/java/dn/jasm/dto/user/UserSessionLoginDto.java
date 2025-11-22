package dn.jasm.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public record UserSessionLoginDto(Long userId,
                                  String username) {
}
