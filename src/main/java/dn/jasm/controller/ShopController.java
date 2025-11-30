package dn.jasm.controller;

import dn.jasm.dto.shop.ListShopResponse;
import dn.jasm.dto.shop.MapShopResponse;
import dn.jasm.dto.shop.ShopRequest;
import dn.jasm.dto.shop.ShopResponse;
import dn.jasm.entity.ShopEntity;
import dn.jasm.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    private static final String GET_SHOP_LIST = "/api/v1/shops";
    private static final String REGISTER_SHOP = "/api/v1/shops/shop/register";
    private static final String GET_SHOP_INFO = "/api/v1/shops/info";
    private static final String BAN_SHOP = "/api/v1/shops/shop/ban";
    private static final String GET_SHOP_BY_ID = "/api/v1/shops/shop/{id}";
    private static final String GET_OWNER_OF_SHOP = "/api/v1/shops/shop/{shopId}/owner";
    private static final String UPDATE_SHOP = "/api/v1/shops/shop/update/{shopId}";
    private static final String FIND_SHOP_BY_SHOP_NAME = "/api/v1/shops/shop/name";
    private static final String GET_SHOP_BALANCE_TO_OWNER = "/api/v1/shops/shop/{shopId}/balance";
    private static final String DELETE_SHOPS_BY_IDS = "/api/v1/shops/delete";

    @GetMapping(value = GET_SHOP_LIST)
    public ListShopResponse getShopList(@RequestParam int pageNumber,
                                        @RequestParam int pageSize){
        return shopService.getListOfShops(pageNumber,pageSize);
    }

    @PostMapping(REGISTER_SHOP)
    public ShopResponse registerShop(@RequestBody ShopRequest shopRequest){
        return shopService.registerShop(shopRequest);
    }

    @GetMapping(GET_SHOP_INFO)
    public MapShopResponse getSortedRatingsOfShops(){
        return shopService.getSortedRatingsOfShops();
    }

    @DeleteMapping(DELETE_SHOPS_BY_IDS)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByIds(@RequestParam List<Long> shopIds){
        shopService.deleteByIds(shopIds);
    }


}
