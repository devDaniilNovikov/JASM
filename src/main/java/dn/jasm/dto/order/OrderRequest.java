package dn.jasm.dto.order;

import dn.jasm.dto.item.ItemRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "OrderRequest", description = "ДТО для создания заказа")
public class OrderRequest {

    @NotNull(message = "amount of order can't be null")
    @Schema(name = "totalAmount", description = "Суммарная стоимость заказа")
    private BigDecimal totalAmount;

    @Schema(name = "discount", description = "Скидка для заказа")
    private BigDecimal discount = BigDecimal.ZERO;

    @Schema(name = "userId", description = "Уникальный идентификатор пользователя, который создает заказ")
    @NotNull(message = "UserId can't be null")
    private Long userId;

    @Schema(name = "quantity", description = "Количество товаров в заказе")
    @NotNull(message = "quantity of items in order can't be null")
    private Integer quantity;

}
