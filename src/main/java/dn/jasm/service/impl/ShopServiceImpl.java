package dn.jasm.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dn.jasm.configuration.redis.CacheNames;
import dn.jasm.dto.shop.ListShopResponse;
import dn.jasm.dto.shop.MapShopResponse;
import dn.jasm.dto.shop.ShopRequest;
import dn.jasm.dto.shop.ShopResponse;
import dn.jasm.entity.ShopEntity;
import dn.jasm.entity.UserEntity;
import dn.jasm.event.shop.ShopEvent;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.repository.ShopRepository;
import dn.jasm.repository.UserRepository;
import dn.jasm.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectsMapper;

    @Value("${spring.cache.redis.time-to-live}")
    private Duration ttl;


    private final ExecutorService executorService = Executors.newFixedThreadPool(10);


    @Override
    public ShopEntity findById(Long id) {
        if (redisTemplate.hasKey(String.valueOf(id))) {
            return shopRepository.findById(id)
                    .orElseThrow(RuntimeException::new);
        }
        var shop = shopRepository.findById((id))
                .orElseThrow(RuntimeException::new);
        redisTemplate.opsForValue().setIfAbsent(String.valueOf(shop.getId()),
                shop.getName(), Duration.ofMinutes(10));
        publishEvent(shop);
        return shopRepository.findById(shop.getId())
                .orElseThrow(RuntimeException::new);
    }

    //    @EventListener
    public void handleShopEvent(ShopEvent shopEvent) {
        log.info("Created new shop, owner of shop: {}, shopId: {}, shopName: {}", shopEvent.getOwnerId(),
                shopEvent.getShopId(), shopEvent.getShopName());
        var shop = mapFromEventToEntity(shopEvent);
        shopRepository.save(shop);
    }

    @Async
    public void publishEvent(
            ShopEntity shop
    ) {
        eventPublisher.publishEvent(new ShopEvent(
                this,
                shop.getId().toString(),
                shop.getOwnerName(),
                shop.getName()
        ));
    }

    private ShopEntity mapFromEventToEntity(ShopEvent shopEvent) {
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

    @Transactional
    @Override
    public ShopResponse registerShop(ShopRequest shopRequest) {
        ShopEntity shop = new ShopEntity();
        UserEntity owner = userRepository.findById(shopRequest.getOwnerId())
                .orElseThrow(UserNotFoundException::new);
        ShopResponse shopResponse = ShopResponse.builder()
                .id(shop.getId())
                .name(shopRequest.getName())
                .category(shopRequest.getCategory())
                .createdAt(shopRequest.getDateOfRegistration())
                .isActive(true)
                .isVerified(true)
                .rating(shopRequest.getRating())
                .ownerName(owner.getUsername())
                .updatedAt(shopRequest.getDateOfRegistration())
                .description(shopRequest.getDescription())
                .totalCashTurnover(BigDecimal.ZERO)
                .itemsIds(new ArrayList<>())
                .buyersIds(new ArrayList<>())
                .countOfSales(0)
                .reviewCounts(0)
                .build();
        shop.setId(shopResponse.getId());
        shop.setName(shopResponse.getName());
        shop.setOwnerName(owner.getUsername());
        shop.setRating(shopResponse.getRating());
        shop.setCountOfSales(shop.getCountOfSales());
        shop.setCreatedAt(LocalDateTime.now());
        shop.setDeposit(BigDecimal.valueOf(1000.0));
        shop.setUpdatedAt(LocalDateTime.now());
        shop.setCountOfSales(0);
        shopRepository.save(shop);
        WeakReference<String> cacheKey = new WeakReference<>(CacheNames.SHOP_CACHE
                .getValue()
                .concat(String.valueOf(shop.getId())));
        var cacheValue = objectsMapper.convertValue(shopResponse, ShopResponse.class);
        redisTemplate.opsForValue()
                .set(Objects.requireNonNull(cacheKey.get()), cacheValue, Duration.ofMinutes(10));
        log.info("Registered shop is: {}", shopResponse);
        return shopResponse;


    }

    @Override
    public void deleteShop(Long id) {
        shopRepository.findById(id)
                .ifPresentOrElse(
                        s -> shopRepository.deleteById(s.getId())
                        , () -> {
                            throw new NoSuchElementException("Магазин не найден");
                        });

    }

    @Override
    public Double getRatingOfShop(Long shopId) {
        return shopRepository.findById(shopId)
                .stream()
                .map(ShopEntity::getRating)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    @Override
    public ListShopResponse getListOfShops(int pageNumber,
                                           int pageSize) {
        ListShopResponse listShopResponse = new ListShopResponse();
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        List<ShopEntity> shops = shopRepository.findAll(pageRequest)
                .stream()
                .peek(shopEntity -> {
                    WeakReference<String> cacheKey = new WeakReference<>(CacheNames.SHOP_LIST
                            .getValue()
                            .concat(String.valueOf(shopEntity.getId())));
                    var redisKey = Objects.requireNonNull(cacheKey.get());
                    var value = objectsMapper.convertValue(shopEntity,
                            new TypeReference<ShopEntity>() {
                            });
                    redisTemplate.opsForValue()
                            .set(redisKey,value,Duration.ofMinutes(10));
                })
                .toList();
        var shopNames = shops.stream()
                .map(ShopEntity::getName)
                .distinct()
                .toList();
        log.error("Getting shops with names: {}",shopNames);
        listShopResponse.setShops(shops);
        return listShopResponse;

    }


    @Override
    public MapShopResponse getSortedRatingsOfShops() {
        String prefix = CacheNames.SHOP_CACHE.getValue();
        Set<String> keys = redisTemplate.scan(
                ScanOptions.scanOptions()
                        .match(prefix)
                        .count(100)
                        .build())
                        .stream()
                        .collect(Collectors.toSet());
        redisTemplate.multi();
        List<Object> keysList = redisTemplate.opsForValue().multiGet(keys);
        if (keysList!=null){
            MapShopResponse mapShopResponse = keysList.stream()
                    .filter(Objects::nonNull)
                    .findFirst()
                    .map(key->objectsMapper.convertValue(key,MapShopResponse.class))
                    .orElse(null);
            if (mapShopResponse!=null){
                log.info("Value getting from cache: {}",mapShopResponse.getShopMap().values());
                return mapShopResponse;
            }
        }
        Map<String, List<ShopEntity>> shops = shopRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        ShopEntity::getName,
                        Collectors.filtering(
                                shopEntity -> shopEntity.getLocation() == null,
                                Collectors.toList())))
                .entrySet()
                .stream()
                .filter(entry -> {
                    boolean firstCondition = !entry.getValue().isEmpty();
                    boolean secondCondition = entry.getValue()
                            .stream()
                            .allMatch(shopEntity -> shopEntity.getRating()>0);
                    boolean thirdCondition = entry.getValue()
                            .stream()
                            .map(ShopEntity::getItems)
                            .flatMap(Collection::stream)
                            .allMatch(Objects::isNull);
                    var trueAt = Boolean.logicalAnd(firstCondition,secondCondition);
                    return Boolean.logicalAnd(trueAt,thirdCondition);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue)
                );
        if (shops.isEmpty()) {
            log.error("Empty map: {}", shops.entrySet());
        }
        MapShopResponse mapShopResponse = new MapShopResponse();
        mapShopResponse.setShopMap(shops);
        redisTemplate.opsForValue().set(prefix,mapShopResponse,Duration.ofMinutes(10));
        return mapShopResponse;
    }



    @Override
    public void deleteByIds(List<Long> shopIds) {
        var idsStringValue = shopIds.stream()
                .map(key -> CacheNames.SHOP_CACHE
                        .getValue()
                        .concat(String.valueOf(key)))
                .toList();
        var dbFuture = CompletableFuture.runAsync(() -> {
            shopRepository.deleteAllByIdInBatch(shopIds);
        }, executorService);
        var cacheFuture = CompletableFuture.runAsync(() -> {
            redisBatchDelete(idsStringValue);
        }, executorService);
        CompletableFuture.allOf(dbFuture, cacheFuture)
                .thenRun(() -> log.info("Successfully deleting from db and cache"))
                .exceptionally(ex -> {
                    log.error("Error during deleting");
                    return null;
                });
    }


    private void redisBatchDelete(List<String> shopIds) {
        if (shopIds.isEmpty()){
            throw new IllegalArgumentException("Id's can't be null");
        }
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String key : shopIds) {
                connection.openPipeline();
                connection.keyCommands().del(key.getBytes(StandardCharsets.UTF_8));
                connection.closePipeline();
            }
            return null;
        });
    }
    private List<Object> redisBatchGet(List<String> keys){
        return redisTemplate.executePipelined((RedisCallback<Object>) redis->{
            for (String key: keys){
                redis.openPipeline();
                redis.stringCommands().get(key.getBytes());
                redis.closePipeline();
            }
            return null;
        });
    }
}
