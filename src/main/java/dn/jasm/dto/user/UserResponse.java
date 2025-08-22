package dn.jasm.dto.user;

import com.fasterxml.jackson.annotation.*;
import dn.jasm.dto.comment.CommentRequest;
import dn.jasm.entity.OrderEntity;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse implements Serializable{

    @Serial
    private static final long serialVersionUID = -5629594248387283631L;

    private Long id;
    @JsonProperty(value = "имя пользователя")
    private String username;
    @JsonIgnore
    private String password;
    @JsonProperty(value = "номер телефона")
    private String phoneNumber;
    @JsonProperty(value = "статус пользователя")
    private String userStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd")
    private LocalDateTime updatedAt;
    @JsonProperty(value = "количество покупок")
    private Integer countOfDeals;
    private List<OrderEntity> orders;
    private Map<String,Object> users;
    private List<CommentRequest> comments;
}
