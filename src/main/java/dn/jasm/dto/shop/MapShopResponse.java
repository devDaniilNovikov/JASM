package dn.jasm.dto.shop;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dn.jasm.dto.item.ItemResponse;
import dn.jasm.entity.ItemEntity;
import dn.jasm.entity.ShopEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "ShopRequest", description = "ДТО для получения данных магазина")
public class MapShopResponse {

    @JsonProperty(value = "Information about shops")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(name = "shopMap", description = "Информация о магазине")
    private Map<String, List<ShopEntity>> shopMap = new ConcurrentHashMap<>();

    @JsonProperty(value = "Items of shop")
    @Schema(name = "shopItemMap", description = "Информация о товарах магазина")
    private Map<String, List<ItemResponse>> shopItemMap = new ConcurrentHashMap<>();

}
