package dn.jasm.dto.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import dn.jasm.dto.item.ItemRequest;
import dn.jasm.dto.item.ItemResponse;
import dn.jasm.entity.ItemEntity;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class OrderResponse implements Serializable {

    @Schema(name = "id", description = "Уникальный идентификатор  заказа")
    private Long id;

    @Schema(name = "totalAmount", description = "Суммарная стоимость заказа")
    private BigDecimal totalAmount;

    @Schema(name = "userId", description = "Уникальный идентификатор пользователя, который создает заказ")
    private Long userId;

    @Schema(name = "itemsNames", description = "Список названий товаров в заказе")
    @ArraySchema(schema = @Schema(implementation = String.class))
    private List<String> itemsNames;

    @Schema(name = "quantity", description = "Количество товаров в заказе")
    private Integer quantity;

    @Schema(name = "status", description = "Cтатус заказа")
    private String status;

    @Schema(name = "isPayed", description = "Статус оплаты заказа")
    private Boolean isPayed;

    @Schema(name = "isShipped", description = "Возможность доставки заказа")
    private Boolean isShipped;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrderResponse that = (OrderResponse) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
