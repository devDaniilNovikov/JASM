package dn.jasm.dto.user;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest implements Serializable {



    @NotEmpty(message = "Username can't be empty")
    @Size(min = 5,max = 20,message = "username size must be size between 5 and 20 chars!")
    private String username;

    @NotEmpty(message = "Password can't be empty")
    @Size(min = 5,max = 50,message = "password size must be size between 5 and 50 chars!")
    private String password;

    @NotEmpty(message = "PhoneNumber can't be empty")
    private String phoneNumber;

    @NotEmpty(message = "Email can't be empty")
    private String email;

    private String orderId;

}
