package dn.jasm.dto.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemRequest {

    @NotBlank(message = "name of item can't be blank")
    private String name;
    private String description;
    private Boolean isShippable;
    @NotBlank(message = "name of item can't be blank")
    private Integer quantity;
    @NotBlank(message = "price can't be blank")
    @NotNull(message = "price can't be null")
    private BigDecimal price;
    @NotBlank(message = "shopId can't be blank")
    @NotNull(message = "shopId can't be null")
    private Long shopId;



}
