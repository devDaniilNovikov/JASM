package dn.jasm.service;

import dn.jasm.dto.shop.ShopRequest;
import dn.jasm.dto.shop.ShopResponse;
import dn.jasm.entity.ShopEntity;

import java.util.List;

public interface ShopService {

    ShopEntity findById(Long id);

    ShopEntity findByName(String shopName);

    void registerShop(ShopRequest shopRequest);

    void deleteShop(Long id);

    Double getRatingOfShop(Long shopId);

    List<ShopResponse> getListOfShops();




}
