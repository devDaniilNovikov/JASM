package dn.jasm.dto.shop;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ShopRequest", description = "ДТО для регистрации/обновления магазина")
public class ShopRequest {

    @NotBlank(message = "name can't be blank")
    @Schema(name = "name", description = "Название магазина", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(name = "description", description = "Описание магазина")
    private String description;

    @Schema(name = "ownerId", description = "Уникальный идентификатор владельца магазина")
    private Long ownerId;

    @Schema(name = "avatarUrl", description = "Ссылка на аватар магазина")
    private String avatarUrl;

    @Schema(name = "deposit", description = "Страховой депозит магазина")
    private BigDecimal deposit = BigDecimal.ZERO;

    @Schema(name = "category", description = "Категория магазина")
    private String category;

    @Schema(name = "rating", description = "Рейтинг магазина")
    private Double rating = 0.0;

    @Schema(name = "dateOfRegistration", description = "Дата регистрации магазина")
    private String dateOfRegistration = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-Mm-Dd"));
}
