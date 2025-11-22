package dn.jasm.service.impl;

import dn.jasm.dto.shop.ListShopResponse;
import dn.jasm.dto.shop.ShopRequest;
import dn.jasm.entity.ShopEntity;
import dn.jasm.event.shop.ShopEvent;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.repository.ShopRepository;
import dn.jasm.repository.UserRepository;
import dn.jasm.service.RedisService;
import dn.jasm.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final RedisService redisService;



    @Override
    public ShopEntity findById(Long id) {
        if (redisService.checkKeyExist(String.valueOf(id))){
            return shopRepository.findById(id)
                    .orElseThrow(RuntimeException::new);
        }
        var shop = shopRepository.findById((id))
                .orElseThrow(RuntimeException::new);
        redisService.writeObjectInRedis(String.valueOf(shop.getId()),
                shop.getName());
        publishEvent(shop);
        return shopRepository.findById(shop.getId())
                .orElseThrow(RuntimeException::new);
    }

//    @EventListener
    public void handleShopEvent(ShopEvent shopEvent){
        log.info("Created new shop, owner of shop: {}, shopId: {}, shopName: {}",shopEvent.getOwnerId(),
                shopEvent.getShopId(),shopEvent.getShopName());
        var shop = mapFromEventToEntity(shopEvent);
        shopRepository.save(shop);
    }

    @Async
    public void publishEvent(
            ShopEntity shop
    ){
        eventPublisher.publishEvent(new ShopEvent(
                this,
                shop.getId().toString(),
                shop.getOwnerName(),
                shop.getName()
        ));
    }

    private ShopEntity mapFromEventToEntity(ShopEvent shopEvent){
        ShopEntity shop = new ShopEntity();
        shop.setId(Long.valueOf(shopEvent.getShopId()));
        var owner = userRepository.findById(
                Long.valueOf(shopEvent.getOwnerId()))
                .orElseThrow(UserNotFoundException::new);
        shop.setOwnerName(owner.getUsername());
        shop.setName(shopEvent.getShopName());
        return shop;
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
