package dn.jasm.dto.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemResponse implements Serializable {
    private Long id;
    private String name;
    private String description;
    private Double rating;
    private Integer discount;
    private BigDecimal price;
    private Integer quantity;
    private Boolean isShippable;


}
