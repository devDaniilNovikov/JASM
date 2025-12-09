package dn.jasm.dto.shop;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dn.jasm.dto.item.ItemResponse;
import dn.jasm.dto.payment.PaymentResponse;
import dn.jasm.entity.ItemEntity;
import dn.jasm.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class ShopResponse implements Serializable {

    @NotNull(message = "id can't be null")
    @Schema(description = "Уникальный идентификатор магазина")
    private Long id;

    @NotBlank(message = "name can't be blank")
    @Schema(description = "Название магазина", minLength = 3, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 3,max = 50,message = "name must have size between 3 and 50 symbols")
    private String name;

    @NotBlank(message = "category can't be blank")
    @Schema(description = "Категория товаров", requiredMode = Schema.RequiredMode.REQUIRED)
    private String category;

    @PositiveOrZero(message = "countOfSales can't be negative")
    @JsonProperty("sales_count")
    @Schema(description = "Количество совершенных продаж", minimum = "0")
    private Integer countOfSales;

    @NotBlank(message = "name_of_owner can' be blank")
    @JsonProperty("name_of_owner")
    @Schema(description = "Имя владельца магазина", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerName;

    @JsonProperty("items_ids")
    @Schema(description = "Список уникальных идентификаторов товаров в магазине")
    @ArraySchema(schema = @Schema(implementation = Long.class))
    private List<Long> itemsIds;

    @Min(value = 0)
    @Max(value = 5)
    @Schema(description = "Рейтинг магазина", minimum = "0", maximum = "5")
    private Double rating;

    @Schema(description = "Описание магазина")
    private String description;

    @JsonProperty("buyers_of_shop_ids")
    @Schema(description = "Список уникальных идентификаторов")
    @ArraySchema(schema = @Schema(implementation = Long.class))
    private List<Long> buyersIds;

    @Schema(description = "Страховой депозит")
    private BigDecimal deposit;

    @Schema(description = "Статус магазина")
    private String shopStatus;

    @DecimalMin("0.0")
    @JsonProperty("total_cash_turnover")
    @Schema(description = "Общий денежный оборот магазина", minimum = "0.0")
    private BigDecimal totalCashTurnover;

    @PositiveOrZero(message = "total count of products can't be negative")
    @Schema(description = "Общее количество товаров в магазине", minimum = "0")
    @JsonProperty("total_count_of_products")
    private Map<String,Integer> totalCountOfProducts;

    @Schema(description = "Количество отзывов")
    @JsonProperty(value = "count_of_reviews")
    private Integer reviewCounts;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd")
    @Schema(description = "Дата регистрации магазина", type = "string", format = "date")
    private String createdAt = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd")
    @Schema(description = "Дата последнего обновления", type = "string", format = "date")
    private String updatedAt;

    @Schema(description = "Предметы магазина")
    @JsonProperty(namespace = "items_of_shop")
    private Map<String,List<ItemResponse>> items;

    @NotNull(message = "verified status can' be null")
    @Schema(description = "Статус верификации магазина", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isVerified;

    @NotNull(message = "status of active can' be null")
    @Schema(description = "Активен ли магазин", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isActive;

    @Schema(description = "Список ID заказов, совершенных в магазине")
    @ArraySchema(schema = @Schema(implementation = Long.class))
    private List<Long> orderIds;

    @Schema(description = "Данные платежей, совершенных в магазине")
    private Map<String, PaymentResponse> paymentsOfShop;

    @JsonProperty(value = "location_of_shop")
    @Schema(name = "Территориальное расположение магазина")
    private String location;


}
