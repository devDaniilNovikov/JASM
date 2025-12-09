package dn.jasm.service;

import dn.jasm.dto.shop.*;
import dn.jasm.entity.ShopEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

public interface ShopService {

    ShopResponse findById(Long id);

    ShopResponse findByShopName(String shopName);

    ShopResponse registerShop(ShopRequest shopRequest);

    void deleteShop(Long id);

    Double getRatingOfShop(Long shopId);

    ListShopResponse getListOfShops(int pageNumber,
                                    int pageSize);

    ShopResponse getBalanceOfShop(Long shopId);

    MapShopResponse getSortedRatingsOfShops();

    void deleteByIds(List<Long> shopIds);

    MapShopResponse getInformationAboutShop(String shopName);

    MapShopResponse getItemsOfShop(String shopName);

    ShopResponse getOwnerOfShop(Long shopId);

    ShopResponse banShop(Long shopId);

    void updateShop(Long shopId,
                    ShopUpdateRequest shopUpdateRequest);






}
