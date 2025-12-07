package dn.jasm.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dn.jasm.configuration.redis.CacheNames;
import dn.jasm.dto.item.ItemRequest;
import dn.jasm.dto.item.ItemResponse;
import dn.jasm.dto.shop.ListShopResponse;
import dn.jasm.dto.shop.MapShopResponse;
import dn.jasm.dto.shop.ShopRequest;
import dn.jasm.dto.shop.ShopResponse;
import dn.jasm.entity.ShopEntity;
import dn.jasm.entity.UserEntity;
import dn.jasm.event.shop.ShopEvent;
import dn.jasm.exception.ItemNotFoundException;
import dn.jasm.exception.ShopNotFoundException;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.mapper.ItemMapper;
import dn.jasm.mapper.ShopMapper;
import dn.jasm.repository.ItemRepository;
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
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectsMapper;
    private final ShopMapper shopMapper;
    private final ItemMapper itemMapper;

    private static final String REDIS_KEYS_PREFIX = "*";

    @Value("${spring.cache.redis.time-to-live}")
    private Duration ttl;


    private final ExecutorService executorService = Executors.newFixedThreadPool(10);


    @Override
    public ShopResponse findById(Long id) {
        if (redisTemplate.hasKey(String.valueOf(id))) {
            return shopMapper.mapToDto(shopRepository.findById(id)
                    .orElseThrow(RuntimeException::new));
        }
        var shop = shopRepository.findById((id))
                .orElseThrow(RuntimeException::new);
        redisTemplate.opsForValue().setIfAbsent(String.valueOf(shop.getId()),
                shop.getName(), Duration.ofMinutes(10));
        publishEvent(shop);
        return shopMapper.mapToDto(shopRepository.findById(shop.getId())
                .orElseThrow(RuntimeException::new));
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
    public ShopResponse findByShopName(String shopName) {
        var cacheKey = CacheNames.SHOP_CACHE.getValue()
                .concat(shopName);
        var value = redisTemplate.opsForValue().get(cacheKey);
        if (redisTemplate.hasKey(cacheKey)) {
            if (value != null) {
                log.info("Value will get from cache");
                return objectsMapper.convertValue(value, ShopResponse.class);
            }
        }
        var shop = shopRepository.findByName(shopName)
                .orElseThrow(ShopNotFoundException::new);
        log.info("Value will get from db");
        var shopDto = shopMapper.mapToDto(shop);
        redisTemplate.opsForValue().set(cacheKey,shopDto,ttl);
        return shopDto;
    }

    @Transactional
    @Override
    public ShopResponse registerShop(ShopRequest shopRequest) {
        ShopEntity shop = shopMapper.mapToEntity(shopRequest);
        shop.setIsActive(true);
        shop.setIsVerified(true);
        shopRepository.save(shop);
        ShopResponse shopResponse = shopMapper.mapToDto(shop);
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

    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();

        redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(100)
                    .build();

            Cursor<byte[]> cursor = connection.scan(options);

            while (cursor.hasNext()) {
                keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
            }

            cursor.close();
            return keys;
        });

        return keys;
    }




    @Override
    public MapShopResponse getSortedRatingsOfShops() {
        var cacheKeyPrefix = CacheNames.SHOP_CACHE.getValue();
        Set<String> keys = scanKeys(cacheKeyPrefix + REDIS_KEYS_PREFIX);
        if (!keys.isEmpty()) {
            List<Object> cachedValues = redisBatchGet(new ArrayList<>(keys));
            log.info("Values from cache: {}",cachedValues);
            Map<String, List<ShopEntity>> shops = new HashMap<>();
            for (Object cachedValue : cachedValues) {
                log.info("Value from cache: {}",cachedValue);
                if (cachedValue != null) {
                    List<ShopEntity> shopList = objectsMapper.convertValue(
                            cachedValue,
                            new TypeReference<List<ShopEntity>>() {}
                    );
                    log.info("New shopList: {}",shopList);
                    if (!shopList.isEmpty()) {
                        String shopName = shopList.get(0).getName();
                        shops.put(shopName, shopList);
                    }
                }
            }

            if (!shops.isEmpty()) {
                log.info("Values were retrieved from cache, total shops: {}", shops.size());
                MapShopResponse mapShopResponse = new MapShopResponse();
                mapShopResponse.setShopMap(shops);
                return mapShopResponse;
            }
        }
        Map<String, List<ShopEntity>> shops = shopRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        ShopEntity::getName,
                        Collectors.filtering(
                                shopEntity -> shopEntity.getRating()<5.0,
                                Collectors.toList())))
                .entrySet()
                .stream()
                .filter(entry -> {
                    boolean firstCondition = !entry.getValue().isEmpty();
                    boolean secondCondition = entry.getValue()
                            .stream()
                            .allMatch(shopEntity -> shopEntity.getLocation() == null);
                    boolean thirdCondition = entry.getValue()
                            .stream()
                            .map(ShopEntity::getItems)
                            .flatMap(Collection::stream)
                            .allMatch(Objects::isNull);
                    var trueAt = Boolean.logicalAnd(firstCondition,secondCondition);
                    return Boolean.logicalAnd(trueAt,thirdCondition);
                })
                .peek(shop->{
                    var key = cacheKeyPrefix+shop.getKey();
                    var redisValue = shop.getValue();
                    var ttl = Duration.ofMinutes(10);
                    redisTemplate.multi();
                    redisTemplate.opsForValue().set(key,redisValue,ttl);
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

    @Override
    public Map<String, ShopEntity> getInformationAboutShop(String shopName) {
        Map<String,ShopEntity> shopMap = new ConcurrentHashMap<>();

        ShopEntity value = shopRepository.findByNameIgnoreCase(shopName)
                .stream()
                .filter(shopEntity -> shopEntity.getRating()>0)
                .collect(Collectors.groupingBy(
                        ShopEntity::getName,
                        Collectors.filtering(s->s.getOwnerName()!=null,
                                Collectors.toList())

                ))
                .values()
                .stream()
                .flatMap(Collection::stream)
                .findAny()
                .orElseThrow(RuntimeException::new);
        shopMap.computeIfAbsent(shopName,v->value);
       redisTemplate.opsForValue().multiSet(shopMap);
        redisTemplate.expire(shopName,10,TimeUnit.MINUTES);
        return shopMap;
    }

    @Override
    public MapShopResponse getItemsOfShop(String shopName) {
        var cacheValue = redisTemplate.opsForValue().get(shopName);
        if (cacheValue!=null){
            log.info("Value was get from cache: {}",cacheValue);
            return objectsMapper.convertValue(cacheValue, MapShopResponse.class);
        }
        var shop = shopRepository.findByName(shopName).orElseThrow();
        var itemsOfShop = shopRepository.findByName(shopName)
                .stream()
                .map(ShopEntity::getItems)
                .flatMap(Collection::stream)
                .distinct()
                .filter(Objects::nonNull)
                .map(itemMapper::mapToDto)
                .toList();
        MapShopResponse mapShopResponse = MapShopResponse.builder()
                .shopItemMap(Map.of(shopName,itemsOfShop))
                .build();
        redisTemplate.opsForValue().set(shopName,
                mapShopResponse,
                Duration.ofMinutes(10));
        log.info("Value was get from db: {}",mapShopResponse.getShopItemMap());
        return mapShopResponse;
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
                redis.stringCommands().get(key.getBytes());
            }
            return null;
        });
    }
}
