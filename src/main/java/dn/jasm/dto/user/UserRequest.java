package dn.jasm.dto.user;


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
//
//    @NotEmpty(message = "Username can't be empty")
//    @Size(min = 5,max = 20,message = "username size must be size between 5 and 20 chars!")
    private String username;

//    @NotEmpty(message = "Password can't be empty")
//    @Size(min = 5,max = 50,message = "password size must be size between 5 and 50 chars!")
    private String password;

//    @NotEmpty(message = "PhoneNumber can't be empty")
    private String phoneNumber;

//    @NotEmpty(message = "Email can't be empty")
    private String email;

    private String orderId;

}
