package dn.jasm.dto.order;

import dn.jasm.entity.ItemEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.*;

@Data
@Builder
@Schema(name = "OrderItemResponse", description = "Список товаров в заказе")
public class OrderItemResponse {

    @Schema(name = "userId", description = "Уникальный идентификатор пользователя, который сделал заказ")
    private Long userId;

    @Schema(name = "count", description = "Количество товаров в заказе")
    private Integer count;

    @Schema(name = "totalAmount", description = "Суммарная стоимость заказа")
    private BigDecimal totalAmount;

    @Schema(name = "items", description = "Список товаров в заказе")
    private List<ItemEntity> items;
}
