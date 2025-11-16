package dn.jasm.dto.order;

import dn.jasm.dto.item.ItemRequest;
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
public class OrderRequest {

    private BigDecimal totalAmount;
    private BigDecimal discount;
    private Long userId;
    private Integer quantity;

}
