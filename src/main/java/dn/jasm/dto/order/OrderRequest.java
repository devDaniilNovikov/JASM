package dn.jasm.dto.order;

import dn.jasm.dto.item.ItemRequest;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequest {

    private BigDecimal totalAmount;
    private BigDecimal discount;
    private Long userId;

}
