package dn.jasm.dto.user;

import com.fasterxml.jackson.annotation.*;
import dn.jasm.dto.card.CardResponse;
import dn.jasm.dto.comment.CommentRequest;
import dn.jasm.entity.CardEntity;
import dn.jasm.entity.OrderEntity;
import dn.jasm.entity.TransactionEntity;
import dn.jasm.entity.UserEntity;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
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
    @JsonProperty(value = "баланс пользователя")
    private BigDecimal balance;
    @JsonProperty(value = "статус пользователя")
    private String userStatus;
    @JsonProperty(value = "почта пользователя")
    private String email;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd")
    private LocalDate createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd")
    private LocalDate updatedAt;
    @JsonProperty(value = "количество покупок")
    private Integer countOfDeals;
    private List<OrderEntity> orders;
    @JsonIgnore
    private Map<String,Object> users;
    private List<CommentRequest> comments;
    @JsonProperty(value = "пользователь и его транзакции")
    private Map<String,List<TransactionEntity>> txMap;
    private Map<String, Set<CardResponse>> cards;
    @JsonIgnore
    private List<UserEntity> userList;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserResponse{");
        sb.append("balance=").append(balance);
        sb.append(", id=").append(id);
        sb.append(", username='").append(username).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", phoneNumber='").append(phoneNumber).append('\'');
        sb.append(", userStatus='").append(userStatus).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", countOfDeals=").append(countOfDeals);
        sb.append(", orders=").append(orders);
        sb.append(", users=").append(users);
        sb.append(", comments=").append(comments);
        sb.append(", txMap=").append(txMap);
        sb.append(", cards=").append(cards);
        sb.append(", userList=").append(userList);
        sb.append('}');
        return sb.toString();
    }
}
