package dn.jasm.dto.item;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "ItemRequest", description = "ДТО для создания предмета")
public class ItemRequest {

    @NotBlank(message = "name of item can't be blank")
    @Schema(name = "name", description = "Название добавляемого предмета")
    private String name;

    @Schema(name = "description", description = "Описание предмета")
    private String description;

    @Schema(name = "isShippable", description = "Возможность доставки предмета")
    private Boolean isShippable;

    @Schema(name = "quantity", description = "Количество предметов на складе")
    @NotBlank(message = "name of item can't be blank")
    private Integer quantity;

    @Schema(name = "price", description = "Цена предмета")
    @NotBlank(message = "price can't be blank")
    @NotNull(message = "price can't be null")
    private BigDecimal price;

    @Schema(name = "shopId", description = "Уникальный идентификатор магазина, которому добавляется предмет")
    @NotBlank(message = "shopId can't be blank")
    @NotNull(message = "shopId can't be null")
    private Long shopId;



}
