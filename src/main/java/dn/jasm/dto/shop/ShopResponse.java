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
    @Schema(name = "id", description = "Уникальный идентификатор магазина")
    private Long id;

    @NotBlank(message = "name can't be blank")
    @Schema(name = "name", description = "Название магазина", minLength = 3, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 3,max = 50,message = "name must have size between 3 and 50 symbols")
    private String name;

    @NotBlank(message = "category can't be blank")
    @Schema(name = "category", description = "Категория товаров", requiredMode = Schema.RequiredMode.REQUIRED)
    private String category;

    @PositiveOrZero(message = "countOfSales can't be negative")
    @JsonProperty("sales_count")
    @Schema(name = "countOfSales", description = "Количество совершенных продаж", minimum = "0")
    private Integer countOfSales;

    @NotBlank(message = "name_of_owner can' be blank")
    @JsonProperty("name_of_owner")
    @Schema(name = "ownerName", description = "Имя владельца магазина", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerName;

    @JsonProperty("items_ids")
    @Schema(name = "itemsIds", description = "Список уникальных идентификаторов товаров в магазине")
    @ArraySchema(schema = @Schema(implementation = Long.class))
    private List<Long> itemsIds;

    @Min(value = 0)
    @Max(value = 5)
    @Schema(name = "rating", description = "Рейтинг магазина", minimum = "0", maximum = "5")
    private Double rating;

    @Schema(name = "description", description = "Описание магазина")
    private String description;

    @JsonProperty("buyers_of_shop_ids")
    @Schema(name = "buyersIds", description = "Список уникальных идентификаторов")
    @ArraySchema(schema = @Schema(implementation = Long.class))
    private List<Long> buyersIds;

    @Schema(name = "deposit", description = "Страховой депозит")
    private BigDecimal deposit;

    @Schema(name = "shopStatus", description = "Статус магазина")
    private String shopStatus;

    @DecimalMin("0.0")
    @JsonProperty("total_cash_turnover")
    @Schema(name = "totalCashTurnover", description = "Общий денежный оборот магазина", minimum = "0.0")
    private BigDecimal totalCashTurnover;

    @PositiveOrZero(message = "total count of products can't be negative")
    @Schema(name = "totalCountOfProducts", description = "Общее количество товаров в магазине", minimum = "0")
    @JsonProperty("total_count_of_products")
    private Map<String,Integer> totalCountOfProducts;

    @Schema(name = "reviewCounts", description = "Количество отзывов")
    @JsonProperty(value = "count_of_reviews")
    private Integer reviewCounts;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd")
    @Schema(name = "createdAt", description = "Дата регистрации магазина", type = "string", format = "date")
    private String createdAt = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd")
    @Schema(name = "updatedAt", description = "Дата последнего обновления", type = "string", format = "date")
    private String updatedAt;

    @Schema(name = "items", description = "Предметы магазина")
    @JsonProperty(namespace = "items_of_shop")
    private Map<String,List<ItemResponse>> items;

    @NotNull(message = "verified status can' be null")
    @Schema(name = "isVerified", description = "Статус верификации магазина", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isVerified;

    @NotNull(message = "status of active can' be null")
    @Schema(name = "isActive", description = "Активен ли магазин", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isActive;

    @Schema(name = "orderIds", description = "Список ID заказов, совершенных в магазине")
    @ArraySchema(schema = @Schema(implementation = Long.class))
    private List<Long> orderIds;

    @Schema(name = "paymentsOfShop", description = "Данные платежей, совершенных в магазине")
    private Map<String, PaymentResponse> paymentsOfShop;

    @JsonProperty(value = "location_of_shop")
    @Schema(name = "location", description = "Территориальное расположение магазина")
    private String location;


}
