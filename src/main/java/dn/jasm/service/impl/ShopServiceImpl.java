package dn.jasm.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dn.jasm.configuration.aop.Loggable;
import dn.jasm.configuration.redis.CacheNames;
import dn.jasm.dto.item.ItemRequest;
import dn.jasm.dto.item.ItemResponse;
import dn.jasm.dto.shop.*;
import dn.jasm.entity.ShopEntity;
import dn.jasm.entity.UserEntity;
import dn.jasm.entity.enums.ShopStatus;
import dn.jasm.event.ItemEvent;
import dn.jasm.event.shop.ShopEvent;
import dn.jasm.exception.ItemNotFoundException;
import dn.jasm.exception.ShopNotFoundException;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.mapper.ItemMapper;
import dn.jasm.mapper.ShopMapper;
import dn.jasm.repository.ItemRepository;
import dn.jasm.repository.ShopRepository;
import dn.jasm.repository.UserRepository;
import dn.jasm.service.LogService;
import dn.jasm.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.*;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
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
    private final LogService logService;

    private static final String REDIS_KEYS_PREFIX = "*";
    private static final long CACHE_TTL = 10;


    private final ExecutorService executorService = Executors.newFixedThreadPool(10);


    @Override
    public ShopResponse findById(Long id) {
        var cacheKey = CacheNames.SHOP_CACHE
                .getValue()
                .concat(id.toString());
        var cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (cacheValue!=null){
            logService.cacheLog(cacheKey,cacheValue);
            return objectsMapper.convertValue(cacheValue,ShopResponse.class);
        }
        var shopFromDb = shopRepository.findById((id))
                .orElseThrow(RuntimeException::new);
        var shopDto = shopMapper.mapToDto(shopFromDb);
        redisTemplate.opsForValue()
                .setIfAbsent(String.valueOf(shopDto.getId()),
                shopDto.getName(),Duration.ofMinutes(10));
        logService.dbLog(shopDto.getId(),shopDto);
        return shopDto;
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
        if (value!=null){
            logService.cacheLog(cacheKey,value);
            return objectsMapper.convertValue(value,ShopResponse.class);
        }

        var shopFromDb = shopRepository.findByName(shopName)
                .orElseThrow(ShopNotFoundException::new);
        var shopDto = shopMapper.mapToDto(shopFromDb);
        logService.dbLog(shopDto.getId(),shopDto);
        redisTemplate.opsForValue()
                .setIfAbsent(cacheKey,shopDto,CACHE_TTL,TimeUnit.MINUTES);
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
        String cacheKey = CacheNames.SHOP_CACHE
                .getValue()
                .concat(shop.getId().toString());
        var cacheValue = objectsMapper.convertValue(shopResponse, ShopResponse.class);
        redisTemplate.opsForValue()
                .setIfAbsent(cacheKey,
                        cacheValue,
                        CACHE_TTL,
                        TimeUnit.MINUTES);
    log.info("Registered shop is: {}", shopResponse);
        return shopResponse;


    }

    @Override
    public void deleteShop(Long id) {
        var cacheKeyForDelete = CacheNames.SHOP_CACHE
                .getValue()
                .concat(id.toString());
        CompletableFuture.runAsync(() -> shopRepository.deleteById(id))
                .thenRunAsync(() -> redisTemplate.opsForValue().get(cacheKeyForDelete))
                .thenRunAsync(() -> redisTemplate.delete(cacheKeyForDelete))
                .exceptionally(r -> {
                    if (r != null) {
                        log.error("Error while during async operation");
                    }
                    return null;
                });
    }

    @Override
    public Double getRatingOfShop(Long shopId) {
        var cacheKey = CacheNames.SHOP_CACHE
                .getValue()
                .concat(shopId.toString());
        var cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (cacheValue!=null){
            logService.cacheLog(cacheKey,cacheValue);
            return objectsMapper.convertValue(cacheValue,ShopResponse.class)
                    .getRating()
                    .describeConstable()
                    .orElse(0.0);
        }
        logService.dbLog(shopId);
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
    public ShopResponse getBalanceOfShop(Long shopId) {
        var cacheKey = CacheNames.SHOP_CACHE
                .getValue()
                .concat(shopId.toString());
        var cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (cacheValue!=null){
            var balanceOfShop = objectsMapper.convertValue(cacheValue,ShopResponse.class).getDeposit();
            return ShopResponse.builder()
                    .deposit(balanceOfShop)
                    .build();
        }
        var balance =  shopRepository.findById(shopId)
                .map(ShopEntity::getDeposit)
                .stream()
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        return ShopResponse.builder()
                .deposit(balance)
                .build();

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
    public MapShopResponse getInformationAboutShop(String shopName) {
        String cacheKey = CacheNames.SHOP_CACHE
                .getValue()
                .concat(shopName);
        Set<String> cacheKeys = scanKeys(cacheKey + REDIS_KEYS_PREFIX);
        List<Object> redisKeys = redisBatchGet(new ArrayList<>(cacheKeys));
        if (!redisKeys.isEmpty()) {
            logService.cacheLog(redisKeys);
            Map<String, List<ShopEntity>> shopMap = new ConcurrentHashMap<>();
            for (Object values : redisKeys) {
                if (values != null) {
                    List<ShopEntity> shops = objectsMapper.convertValue(
                            values,
                            new TypeReference<>() {
                            });
                    if (!shops.isEmpty()) {
                        String shopName_ = shops.get(0).getName();
                        shopMap.put(shopName_, shops);
                    }
                }
            }
            if (!shopMap.isEmpty()) {
                MapShopResponse mapShopResponse = new MapShopResponse();
                mapShopResponse.setShopMap(shopMap);
                return mapShopResponse;
            }

        }

        Map<String, List<ShopEntity>> shopMap = shopRepository.findByNameIgnoreCase(shopName)
                .stream()
                .filter(shopEntity -> shopEntity.getRating() > 0)
                .collect(Collectors.groupingBy(
                        ShopEntity::getName,
                        Collectors.filtering(s -> s.getOwnerName() != null,
                                Collectors.toList())

                ))
                .entrySet()
                .stream()
                .peek(map -> {
                    var redisKey = cacheKey.concat(map.getKey());
                    var redisValue = map.getValue();
                    redisTemplate.opsForValue().setIfAbsent(
                            redisKey, redisValue, CACHE_TTL, TimeUnit.MINUTES
                    );
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
        if (!shopMap.isEmpty()) {
            MapShopResponse mapShopResponse = new MapShopResponse();
            mapShopResponse.setShopMap(shopMap);
            return mapShopResponse;
        }
        return null;
    }

    @Override
    public MapShopResponse getItemsOfShop(String shopName) {
        var cacheKey = CacheNames.ITEM_CACHE
                .getValue()
                .concat(shopName);
        var cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (cacheValue!=null){
            logService.cacheLog(cacheKey,cacheValue);
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
        logService.dbLog(mapShopResponse.getShopItemMap());
        return mapShopResponse;
    }

    @Override
    public ShopResponse getOwnerOfShop(Long shopId) {
        var cacheKey = CacheNames.SHOP_CACHE
                .getValue()
                .concat(shopId.toString());
        var cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (cacheValue!=null){
            logService.cacheLog(cacheKey,cacheValue);
            String ownerName =  objectsMapper.convertValue(cacheValue,ShopResponse.class).getOwnerName();
            return ShopResponse.builder()
                    .ownerName(ownerName)
                    .build();
        }
        var ownerName = shopRepository.findById(shopId)
                .stream()
                .map(shop-> shop.getUser().getUsername())
                .findFirst()
                .orElseThrow(()->new UserNotFoundException("User not found"));
        return ShopResponse.builder()
                .ownerName(ownerName)
                .build();
    }

    @Override
    public ShopResponse banShop(Long shopId) {
        var updatedShop = shopRepository.findById(shopId)
                .stream()
                .peek(shopEntity -> {
                     shopEntity.setStatus(ShopStatus.BANNED);
                     log.info("Updated shop status: {}",shopEntity.getStatus().name());
                     shopRepository.save(shopEntity);
                })
                .map(shopMapper::mapToDto)
                .findFirst()
                .orElseThrow(ShopNotFoundException::new);
        return ShopResponse.builder()
                .shopStatus(updatedShop.getShopStatus())
                .build();
    }

    @Override
    @Transactional
    public void updateShop(Long shopId, ShopUpdateRequest shopUpdateRequest) {
        var cacheKey = CacheNames.SHOP_CACHE
                        .getValue()
                        .concat(shopId.toString());
        var cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (cacheValue!=null){
            var shopForUpdate = objectsMapper.convertValue(cacheValue,ShopEntity.class);
            shopForUpdate.setName(shopUpdateRequest.getName());
            shopForUpdate.setCategory(shopUpdateRequest.getCategory());
            shopForUpdate.setDeposit(shopUpdateRequest.getDeposit());
            shopForUpdate.setDescription(shopUpdateRequest.getDescription());
            shopForUpdate.setUpdatedAt(LocalDateTime.parse(
                    shopUpdateRequest.getDateOfUpdate()
            ));
            shopRepository.save(shopForUpdate);
            log.info("Updated shop with id: {}",shopForUpdate.getId());
        }

        shopRepository.findById(shopId)
                .ifPresentOrElse(shopEntity -> {
                    shopEntity.setName(shopUpdateRequest.getName());
                    shopEntity.setCategory(shopUpdateRequest.getCategory());
                    shopEntity.setDeposit(shopUpdateRequest.getDeposit());
                    shopEntity.setUser(
                            userRepository.findById(shopUpdateRequest.getOwnerId())
                                    .orElseThrow(UserNotFoundException::new)
                    );
                    shopEntity.setOwnerName(shopEntity.getUser().getUsername());
                    shopEntity.setUpdatedAt(LocalDateTime.parse(
                            shopUpdateRequest.getDateOfUpdate()
                    ));
                    shopRepository.save(shopEntity);
                },()->{
                    throw new ShopNotFoundException(MessageFormat.format(
                            "Shop with id: {0} not found",shopId));
                });
    }

    @EventListener
    @Loggable
    @Override
    public void handleCreateItem(ItemEvent itemEvent) {
        log.info("New item in shop: {}",itemEvent.getName());
    }


    private void redisBatchDelete(List<String> shopIds) {
        if (shopIds.isEmpty()){
            throw new IllegalArgumentException("Id's can't be null");
        }
        redisTemplate.multi();
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
        redisTemplate.multi();
        return redisTemplate.executePipelined((RedisCallback<Object>) redis->{
            for (String key: keys){
                redis.stringCommands().get(key.getBytes());
            }
            return null;
        });
    }
}
