package dn.jasm.service.impl;

import dn.jasm.dto.shop.ListShopResponse;
import dn.jasm.dto.shop.ShopRequest;
import dn.jasm.entity.ShopEntity;
import dn.jasm.repository.ShopRepository;
import dn.jasm.repository.UserRepository;
import dn.jasm.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;



    @Override
    public ShopEntity findById(Long id) {
        return null;
    }

    @Override
    public ShopEntity findByShopName(String shopName) {
        return null;
    }

    @Override
    public void registerShop(ShopRequest shopRequest) {

    }

    @Override
    public void deleteShop(Long id) {

    }

    @Override
    public Double getRatingOfShop(Long shopId) {
        return 0.0;
    }

    @Override
    public ListShopResponse getListOfShops() {
        return null;
    }
}
