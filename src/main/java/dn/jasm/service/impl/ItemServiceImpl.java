package dn.jasm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Price;
import com.stripe.model.Product;
import dn.jasm.dto.item.ItemRequest;
import dn.jasm.dto.item.ItemResponse;
import dn.jasm.entity.ItemEntity;
import dn.jasm.entity.OrderEntity;
import dn.jasm.exception.ItemNotFoundException;
import dn.jasm.mapper.ItemMapper;
import dn.jasm.repository.ItemRepository;
import dn.jasm.repository.OrderRepository;
import dn.jasm.repository.ShopRepository;
import dn.jasm.service.ItemService;
import dn.jasm.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectsMapper;
    private final ShopRepository shopRepository;
    private final ItemMapper itemMapper;
    private final RedisTemplate<String,Object> redisTemplate;
    private static final long CACHE_TTL = 10;


    @Transactional
    @Override
    public void addItem(ItemRequest itemRequest){
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setName(itemRequest.getName());
        itemEntity.setIsShippable(true);
        itemEntity.setDescription(itemRequest.getDescription());
        itemEntity.setPrice(itemRequest.getPrice());
        itemEntity.setQuantity(itemRequest.getQuantity());
        var shop = shopRepository.findById(itemRequest.getShopId())
                        .orElseThrow(RuntimeException::new);
        itemEntity.setShop(shop);
        itemRepository.save(itemEntity);
        var itemDto = itemMapper.mapToDto(itemEntity);
        redisTemplate.opsForValue().set(itemDto.getId().toString(),
                itemDto,
                CACHE_TTL,
                TimeUnit.MINUTES);
        log.info("Added item: {}",itemDto);
    }

    @Override
    public ItemResponse getItemById(Long itemId) {
        var cacheValue = redisTemplate.opsForValue().get(String.valueOf(itemId));
        if (cacheValue!=null){
            log.info("Value return from cache");
            return objectsMapper.convertValue(cacheValue,ItemResponse.class);
        }
        log.info("Value return from db");
        return itemRepository.findById(itemId)
                .stream()
                .map(item->{
                  var it = itemMapper.mapToDto(item);
                  redisTemplate.opsForValue().set(it.getId().toString(),it,CACHE_TTL);
                  log.info("Cached item: {}",it);
                  return it;
                })
                .findFirst()
                .orElseThrow(ItemNotFoundException::new);
    }


}


