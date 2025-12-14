package dn.jasm.dto.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ShopUpdateRequest", description = "ДТО для обновления магазина")
public class ShopUpdateRequest {


    @Schema(name = "shopId", description = "Уникальный идентификатор магазина", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long shopId;

    @Schema(name = "name", description = "Название магазина", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(name = "description", description = "Описание магазина")
    private String description;

    @Schema(name = "ownerId", description = "Уникальный идентификатор владельца магазина")
    private Long ownerId;

    @Schema(name = "deposit", description = "Страховой депозит магазина")
    private BigDecimal deposit;

    @Schema(name = "category", description = "Категория магазина")
    private String category;

    @Schema(name = "dateOfUpdate", description = "Дата обновления магазина")
    private String dateOfUpdate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-Mm-Dd"));
}
