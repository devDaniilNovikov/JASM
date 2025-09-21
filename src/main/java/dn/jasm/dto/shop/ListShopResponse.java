package dn.jasm.dto.shop;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ListShopResponse {

    private List<ShopResponse> shops = new ArrayList<>();
}
