package dn.jasm.dto.user;

import com.fasterxml.jackson.annotation.*;
import dn.jasm.dto.card.CardResponse;
import dn.jasm.dto.comment.CommentRequest;
import dn.jasm.entity.CardEntity;
import dn.jasm.entity.OrderEntity;
import dn.jasm.entity.TransactionEntity;
import dn.jasm.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "UserResponse", description = "Данные пользователя")
public class UserResponse implements Serializable{

    @Serial
    private static final long serialVersionUID = -5629594248387283631L;

    @Schema(name = "id",description = "Уникальный идентификатор пользователя")
    private Long id;

    @Schema(name = "username",description = "Имя пользователя")
    private String username;

    @JsonIgnore
    @Schema(name = "password",description = "Пароль пользователя")
    private String password;

    @Schema(name = "phoneNumber",description = "Номер телефона")
    private String phoneNumber;

    @Schema(name = "balance", description = "Баланс пользователя")
    private BigDecimal balance;

    @Schema(name = "userStatus", description = "Статус пользователя")
    private String userStatus;

    @Schema(name = "email", description = "Почта пользователя")
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd")
    @Schema(name = "createdAt", description = "Дата регистрации пользователя")
    private LocalDate createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd")
    @Schema(name = "updatedAt", description = "Дата обновления пользователя")
    private LocalDate updatedAt;

    @Schema(name = "countOfDeals", description = "Количество покупок пользователя")
    private Integer countOfDeals;

    @Schema(name = "orders", description = "Заказы пользователя")
    private List<OrderEntity> orders;

    @JsonIgnore
    @Schema(name = "users", description = "Коллекция пользователей по их никнеймам")
    private Map<String,Object> users;

    @Schema(name = "comments", description = "Комментарии пользователя")
    private List<CommentRequest> comments;

    @Schema(name = "txMap", description = "Транзакции пользователя")
    private Map<String,List<TransactionEntity>> txMap;

    @Schema(name = "cards", description = "Карты пользователя")
    private Map<String, Set<CardResponse>> cards;

    @JsonIgnore
    @Schema(name = "txMap", description = "Список пользователей")
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
