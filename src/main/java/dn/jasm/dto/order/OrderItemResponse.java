package dn.jasm.dto.order;

import dn.jasm.entity.ItemEntity;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.*;

@Data
@Builder
public class OrderItemResponse {

    private Long userId;
    private Integer count;
    private BigDecimal totalAmount;
    private List<ItemEntity> items = new ArrayList<>();
}
