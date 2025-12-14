package dn.jasm.dto.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "ItemResponse", description = "Уникальный идентификатор магазина, которому добавляется предмет")
public class ItemResponse implements Serializable {

    @Schema(name = "shopId", description = "Уникальный идентификатор магазина, которому добавляется предмет")
    private Long id;

    @Schema(name = "name", description = "Название добавляемого предмета")
    private String name;

    @Schema(name = "description", description = "Описание предмета")
    private String description;

    @Schema(name = "rating", description = "Рейтинг предмета")
    private Double rating;

    @Schema(name = "discount", description = "Скидка для предмета")
    private Integer discount;

    @Schema(name = "price", description = "Цена предмета")
    private BigDecimal price;

    @Schema(name = "quantity", description = "Количество предметов на складе")
    private Integer quantity;

    @Schema(name = "price", description = "Цена предмета")
    private Boolean isShippable;


}
