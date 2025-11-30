package dn.jasm.service;

import dn.jasm.dto.shop.ListShopResponse;
import dn.jasm.dto.shop.MapShopResponse;
import dn.jasm.dto.shop.ShopRequest;
import dn.jasm.dto.shop.ShopResponse;
import dn.jasm.entity.ShopEntity;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

public interface ShopService {

    ShopEntity findById(Long id);

    ShopEntity findByShopName(String shopName);

    ShopResponse registerShop(ShopRequest shopRequest);

    void deleteShop(Long id);

    Double getRatingOfShop(Long shopId);

    ListShopResponse getListOfShops(int pageNumber,
                                    int pageSize);

    MapShopResponse getSortedRatingsOfShops();

    void deleteByIds(List<Long> shopIds);




}
