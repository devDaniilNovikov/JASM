package dn.jasm.dto.order;

import dn.jasm.dto.item.ItemRequest;
import dn.jasm.entity.ItemEntity;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.*;

@Data
@Builder
public class OrderResponse {

    private Long id;
    private BigDecimal totalAmount;
    private Long userId;
    private List<ItemEntity> items;
    private Integer quantity;
    private Boolean isPayed;
    private Boolean isShipped;
}
