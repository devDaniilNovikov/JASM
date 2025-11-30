package dn.jasm.dto.shop;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dn.jasm.entity.ShopEntity;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MapShopResponse {

    @JsonProperty(value = "Information about shops")
    private Map<String, List<ShopEntity>> shopMap = new ConcurrentHashMap<>();

}
