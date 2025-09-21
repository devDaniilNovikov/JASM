package dn.jasm.dto.item;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemResponse {
    private Long id;
    private String name;
    private String description;
    private Double rating;
    private Integer discount;
    private BigDecimal price;
    private Integer quantity;
    private Boolean isShippable;


}
