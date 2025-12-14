package dn.jasm.dto.user;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserRequest", description = "ДТО для регистрации пользователя")
public class UserRequest implements Serializable {



    @PostConstruct
    private long getSerialVersionUID() {
        try {
            return SecureRandom.getInstanceStrong().nextLong(1000000000);
        } catch (NoSuchAlgorithmException e) {
            log.error("Can't get require serial version uid, exception is: {}",e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    @NotEmpty(message = "Username can't be empty")
    @Size(min = 5,max = 20,message = "username size must be size between 5 and 20 chars!")
    @Schema(name = "username", description = "Никнейм пользователя")
    private String username;

    @NotEmpty(message = "Password can't be empty")
    @Size(min = 5,max = 50,message = "password size must be size between 5 and 50 chars!")
    @Schema(name = "password", description = "Пароль пользователя")
    private String password;

    @NotEmpty(message = "PhoneNumber can't be empty")
    @Schema(name = "phoneNumber", description = "Номер телефона пользователя")
    private String phoneNumber;

    @Nullable
    @Schema(name = "email", description = "Почта пользователя")
    private String email;

    @Nullable
    @Schema(name = "cardNumber", description = "Номер банковской карты пользователя")
    private String cardNumber;



}
