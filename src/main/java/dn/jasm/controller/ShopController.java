package dn.jasm.controller;

import dn.jasm.dto.shop.*;
import dn.jasm.entity.ShopEntity;
import dn.jasm.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    private static final String GET_SHOP_LIST = "/api/v1/shops";
    private static final String REGISTER_SHOP = "/api/v1/shops/shop/register";
    private static final String GET_SHOP_INFO = "/api/v1/shops/info";
    private static final String DELETE_SHOP_BY_ID = "/api/v1/shops/shop/delete";
    private static final String BAN_SHOP = "/api/v1/shops/shop/ban";
    private static final String GET_SHOP_BY_ID = "/api/v1/shops/shop/{id}";
    private static final String GET_OWNER_OF_SHOP = "/api/v1/shops/shop/{shopId}/owner";
    private static final String UPDATE_SHOP = "/api/v1/shops/shop/{shopId}/update";
    private static final String FIND_SHOP_BY_SHOP_NAME = "/api/v1/shops/shop/name";
    private static final String GET_SHOP_BALANCE_TO_OWNER = "/api/v1/shops/shop/{shopId}/balance";
    private static final String DELETE_SHOPS_BY_IDS = "/api/v1/shops/delete";
    private static final String GET_INFO_ABOUT_SHOP = "/api/v1/shops/shop/info";
    private static final String GET_ITEMS_OF_SHOP = "/api/v1/shops/items";
    private static final String GET_RATING_OF_SHOP = "/api/v1/shops/shop/{id}/rating";

    @GetMapping(value = GET_SHOP_LIST)
    @ResponseStatus(value = HttpStatus.OK)
    public ListShopResponse getShopList(@RequestParam int pageNumber,
                                        @RequestParam int pageSize){
        return shopService.getListOfShops(pageNumber,pageSize);
    }

    @GetMapping(GET_SHOP_BALANCE_TO_OWNER)
    public ShopResponse getBalanceOfShop(@PathVariable Long shopId){
        return shopService.getBalanceOfShop(shopId);
    }

    @PatchMapping(UPDATE_SHOP)
    @ResponseStatus(HttpStatus.UPGRADE_REQUIRED)
    public void updateShop(@PathVariable Long shopId,
                           @RequestBody ShopUpdateRequest shopUpdateRequest){
        shopService.updateShop(shopId,shopUpdateRequest);
    }

    @GetMapping(GET_OWNER_OF_SHOP)
    @ResponseStatus(HttpStatus.OK)
    public ShopResponse getOwnerOfShop(@PathVariable Long shopId){
        return shopService.getOwnerOfShop(shopId);
    }

    @PatchMapping(BAN_SHOP)
    @ResponseStatus(HttpStatus.OK)
    public ShopResponse banShop(@RequestParam Long shopId){
        return shopService.banShop(shopId);
    }




    @GetMapping(GET_RATING_OF_SHOP)
    @ResponseStatus(HttpStatus.OK)
    public Double getRatingOfShop(@PathVariable Long id){
        return shopService.getRatingOfShop(id);
    }

    @GetMapping(GET_SHOP_BY_ID)
    @ResponseStatus(value = HttpStatus.OK)
    public ShopResponse getShopById(@PathVariable Long id){
        return shopService.findById(id);
    }

    @PostMapping(REGISTER_SHOP)
    @ResponseStatus(value = HttpStatus.CREATED)
    public ShopResponse registerShop(@RequestBody ShopRequest shopRequest){
        return shopService.registerShop(shopRequest);
    }

    @GetMapping(GET_ITEMS_OF_SHOP)
    @ResponseStatus(value = HttpStatus.OK)
    public MapShopResponse getItemsOfShop(@RequestParam String shopName){
        return shopService.getItemsOfShop(shopName);
    }

    @GetMapping(FIND_SHOP_BY_SHOP_NAME)
    @ResponseStatus(value = HttpStatus.OK)
    public ShopResponse getShopByName(@RequestParam String shopName){
        return shopService.findByShopName(shopName);
    }

    @GetMapping(GET_SHOP_INFO)
    @ResponseStatus(value = HttpStatus.OK)
    public MapShopResponse getSortedRatingsOfShops(){
        return shopService.getSortedRatingsOfShops();
    }

    @DeleteMapping(DELETE_SHOPS_BY_IDS)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByIds(@RequestParam List<Long> shopIds){
        shopService.deleteByIds(shopIds);
    }

    @DeleteMapping(DELETE_SHOP_BY_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShopById(@RequestParam Long id){
        shopService.deleteShop(id);
    }

    @GetMapping(GET_INFO_ABOUT_SHOP)
    public MapShopResponse getInformationAboutShop(String shopName){
        return shopService.getInformationAboutShop(shopName);
    }


}
