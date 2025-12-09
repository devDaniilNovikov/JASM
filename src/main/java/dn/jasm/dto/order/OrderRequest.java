package dn.jasm.dto.order;

import dn.jasm.dto.item.ItemRequest;
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
public class OrderRequest {

    @NotNull(message = "amount of order can't be null")
    private BigDecimal totalAmount;
    private BigDecimal discount;
    @NotNull(message = "UserId can't be null")
    private Long userId;
    @NotBlank(message = "quantity of items in order can't be blank")
    private Integer quantity;

}
