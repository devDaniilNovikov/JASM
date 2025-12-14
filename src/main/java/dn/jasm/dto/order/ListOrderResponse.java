package dn.jasm.dto.order;

import dn.jasm.entity.ItemEntity;
import dn.jasm.entity.OrderEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.*;

@Data
@ToString
@Schema(name = "ListOrderResponse", description = "Список заказов")
public class ListOrderResponse implements Serializable {

    @Schema(name = "orders", description = "Список запрашиваемых заказов")
    private List<OrderResponse> orders = new ArrayList<>();

}
