package dn.jasm.dto.shop;

import dn.jasm.entity.ShopEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Schema(name = "ListShopResponse", description = "Список магазинов", requiredMode = Schema.RequiredMode.REQUIRED)
public class ListShopResponse {

    @Schema(name = "shops", description = "Список запрашиваемых магазинов", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ShopEntity> shops = new ArrayList<>();
}
