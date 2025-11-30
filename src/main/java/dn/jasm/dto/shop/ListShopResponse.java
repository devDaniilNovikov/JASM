package dn.jasm.dto.shop;

import dn.jasm.entity.ShopEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ListShopResponse {

    private List<ShopEntity> shops = new ArrayList<>();
}
