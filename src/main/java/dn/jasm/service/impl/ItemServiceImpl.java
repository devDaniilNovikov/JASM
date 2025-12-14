package dn.jasm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Price;
import com.stripe.model.Product;
import dn.jasm.configuration.redis.CacheNames;
import dn.jasm.dto.item.ItemRequest;
import dn.jasm.dto.item.ItemResponse;
import dn.jasm.dto.item.ListItemResponse;
import dn.jasm.entity.ItemEntity;
import dn.jasm.entity.OrderEntity;
import dn.jasm.event.ItemEvent;
import dn.jasm.exception.AlreadyExistException;
import dn.jasm.exception.ItemNotFoundException;
import dn.jasm.exception.ShopNotFoundException;
import dn.jasm.mapper.ItemMapper;
import dn.jasm.repository.ItemRepository;
import dn.jasm.repository.OrderRepository;
import dn.jasm.repository.ShopRepository;
import dn.jasm.service.ItemService;
import dn.jasm.service.LogService;
import dn.jasm.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final LogService logService;
    private final ObjectMapper objectsMapper;
    private final ShopRepository shopRepository;
    private final ItemMapper itemMapper;
    private final RedisTemplate<String,Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private static final long CACHE_TTL = 10;
    private static final long MAX_KEYS_COUNT = 10_000;
    private static final String REDIS_KEY_PREFIX = "*";

    @Transactional
    @Override
    public void addItem(ItemRequest itemRequest){
        var itemEntity  = itemMapper.mapToEntity(itemRequest);
        if (itemRepository.existsByNameIgnoreCase(itemRequest.getName())){
            log.error("Item with name already have in shop: {}",itemRequest.getName());
            throw new AlreadyExistException("Please change name of your item");
        }
        itemRepository.save(itemEntity);
        var itemDto = itemMapper.mapToDto(itemEntity);
        var cacheKey = CacheNames.ITEM_CACHE
                        .getValue()
                        .concat(itemDto.getId()
                                .toString());
        redisTemplate.opsForValue()
                .setIfAbsent(cacheKey,
                itemDto,
                CACHE_TTL,
                TimeUnit.MINUTES);
        eventPublisher.publishEvent(new ItemEvent(
                this,
                itemDto.getId().toString(),
                itemDto.getName(),
                itemDto.getPrice(),
                itemDto.getQuantity(),
                itemDto.getDescription(),
                itemEntity.getShop().getId()
        ));
        log.info("Added item: {}",itemDto);
    }

    @Override
    public ItemResponse getItemById(Long itemId) {
        var cacheKey = CacheNames.ITEM_CACHE
                .getValue()
                .concat(itemId.toString());
        var cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (cacheValue != null) {
            logService.cacheLog(itemId.toString(), cacheValue);
            return objectsMapper.convertValue(cacheValue, ItemResponse.class);
        }
        logService.dbLog(itemId);
        return itemRepository.findById(itemId)
                .stream()
                .map(item -> {
                    var it = itemMapper.mapToDto(item);
                    redisTemplate.opsForValue().set(it.getId().toString(), it, CACHE_TTL);
                    logService.cacheLog(it.getId().toString(), it);
                    return it;
                })
                .findFirst()
                .orElseThrow(() -> new ItemNotFoundException(
                        MessageFormat.format("Item with id: {0} not found", itemId)));
    }

    @Override
    public ListItemResponse findAll(int pageSize, int pageNumber) {
        Set<String> keys = redisTemplate.scan(ScanOptions.scanOptions()
                .match(REDIS_KEY_PREFIX)
                .count(MAX_KEYS_COUNT)
                .build())
                .stream()
                .filter(o->o.startsWith(CacheNames.ITEM_LIST.getValue()))
                .collect(Collectors.toSet());
                log.info("Redis keys is: {}",keys);
        if (!keys.isEmpty()){
            List<ItemResponse> cacheValues = Optional.ofNullable(
                    redisTemplate.opsForValue()
                            .multiGet(keys))
                    .stream()
                    .flatMap(Collection::stream)
                    .map(it -> objectsMapper.convertValue(it, ItemResponse.class))
                    .toList();
            ListItemResponse listItemResponse = new ListItemResponse();
            listItemResponse.setItems(cacheValues);
            logService.cacheLog(keys,cacheValues);
            return listItemResponse;
        }
        var pageRequest = PageRequest.of(pageSize, pageNumber);
        List<ItemResponse> items = itemRepository.findAll(pageRequest)
                .stream()
                .map(itemMapper::mapToDto)
                .toList();
        for (ItemResponse itemResponse:items){
            redisTemplate.multi();
            var cacheKey = CacheNames.ITEM_LIST
                    .getValue()
                    .concat(itemResponse.getId().toString());
            redisTemplate.opsForValue()
                    .set(cacheKey, itemResponse,CACHE_TTL, TimeUnit.MINUTES);
        }
        ListItemResponse listItemResponse = new ListItemResponse();
        listItemResponse.setItems(items);
        logService.dbLog(items);
        return listItemResponse;

    }


}


