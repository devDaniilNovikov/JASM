package dn.jasm.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dn.jasm.configuration.redis.CacheNames;
import dn.jasm.dto.item.ItemRequest;
import dn.jasm.dto.item.ItemResponse;
import dn.jasm.dto.order.OrderMapResponse;
import dn.jasm.dto.order.OrderRequest;
import dn.jasm.event.OrderCreateEvent;
import dn.jasm.exception.ItemNotFoundException;
import dn.jasm.mapper.ItemMapper;
import dn.jasm.mapper.OrderMapper;
import dn.jasm.service.KafkaService;
import dn.jasm.dto.order.ListOrderResponse;
import dn.jasm.dto.order.OrderResponse;
import dn.jasm.entity.OrderEntity;
import dn.jasm.exception.OrderNotFoundException;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.entity.ItemEntity;
import dn.jasm.entity.enums.OrderStatus;
import dn.jasm.repository.ItemRepository;
import dn.jasm.repository.OrderRepository;
import dn.jasm.entity.UserEntity;
import dn.jasm.repository.UserRepository;
import dn.jasm.service.LogService;
import dn.jasm.service.OrderService;
import dn.jasm.service.cache.OrderCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Value("${order.discount.value}")
    private BigDecimal discountValue;

    @Value("${order.limit.value}")
    private BigDecimal limit;

    private static final long MAX_LIMIT_OF_ORDERS_FOR_ONE_REQUEST = 5L;
    private static final long CACHE_TTL = 10;
    private static final String REDIS_KEY_PREFIX = "*";
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderMapper orderMapper;
    private final OrderCacheService orderCacheService;
    private final ObjectMapper orderObjectMapper;
    private final KafkaService kafkaService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LogService logService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(30);


    private final Map<String, ListOrderResponse> orderWithUsernameOfOwner = new HashMap<>();


    @Override
    public BigDecimal calculateTotalAmountOfOrder(List<ItemEntity> items) {
        return items.stream()
                .map(ItemEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public ListOrderResponse findAll(int pageSize, int pageNumber) {
        Set<String> cacheKeys = redisTemplate.scan(
                        ScanOptions.scanOptions()
                                .match(REDIS_KEY_PREFIX)
                                .count(1000)
                                .build())
                .stream()
                .filter(key -> key.startsWith(CacheNames.ORDER_CACHE.getValue()))
                .collect(Collectors.toSet());
        var cacheValues = redisTemplate.opsForValue().multiGet(cacheKeys);
        if (cacheValues != null && cacheValues.stream()
                .allMatch(Objects::nonNull)) {
            var orderCacheList = cacheValues.stream()
                    .map(order -> orderObjectMapper.convertValue(order, OrderResponse.class))
                    .toList();
            ListOrderResponse listOrderResponse = new ListOrderResponse();
            listOrderResponse.setOrders(orderCacheList);
            logService.cacheLog(cacheKeys, cacheValues);
            return listOrderResponse;
        }
        PageRequest pageRequest = PageRequest.of(pageSize, pageNumber);
        List<OrderResponse> responses = orderRepository.findAll(pageRequest)
                .stream()
                .map(orderMapper::mapToDto)
                .peek(order -> redisTemplate.opsForValue()
                        .setIfAbsent(
                                CacheNames.ORDER_CACHE
                                        .getValue()
                                        .concat(order.getId()
                                                .toString()),
                                order,
                                CACHE_TTL,
                                TimeUnit.MINUTES
                        ))
                .toList();
        ListOrderResponse listOrderResponse = new ListOrderResponse();
        listOrderResponse.setOrders(responses);
        return listOrderResponse;
    }


    @Override
    @Transactional
    public void cancelOrder(Long orderId,
                            Long userId) {
        var cacheKey = CacheNames.ORDER_CACHE
                .getValue()
                .concat(orderId.toString());
        var cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (cacheValue != null) {
            logService.cacheLog(cacheKey, cacheValue);
            var orderForCancel = orderObjectMapper.convertValue(cacheValue, OrderResponse.class);
            orderForCancel.setStatus(OrderStatus.CANCELLED.getValue());
            var orderForSave = orderMapper.mapToEntity(orderForCancel);
            redisTemplate.multi();
            var future_1 = CompletableFuture.supplyAsync(() -> {
                return orderRepository.save(orderForSave);
            }, executorService);
            var future_2 = CompletableFuture.supplyAsync(() -> {
                return redisTemplate.opsForValue()
                        .setIfAbsent(cacheKey,
                                orderForCancel,
                                CACHE_TTL,
                                TimeUnit.MINUTES);
            }, executorService);
            var result = future_1.thenAcceptBoth(future_2, (o, s) -> {
                log.info("Futures completed successfully");
            });
            try {
                result.get(5, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException | InterruptedException e) {
                log.error("Can't complete future result cause: {}", e.getLocalizedMessage());
            }
//            CompletableFuture.runAsync(()->orderRepository.save(orderForSave),
//                    executorService)
//                    .thenRunAsync(()->redisTemplate.opsForValue()
//                            .setIfAbsent(cacheKey,
//                                    orderForCancel,
//                                    CACHE_TTL,
//                                    TimeUnit.MINUTES),
//                            executorService)
//                    .exceptionally(r->{
//                        if (r!=null){
//                            log.error("Error in async jobs cause: {}",r.getMessage());
//                        }
//                        return null;
//                    });
        }
        var order = userRepository.findById(userId)
                .stream()
                .map(u -> {
                    var orders = u.getOrders();
                    log.info("Orders is: {}", orders.stream()
                            .map(OrderEntity::getId)
                            .toList());
                    return orders;
                })
                .flatMap(Collection::stream)
                .filter(o -> o.getId().equals(orderId))
                .peek(it -> {
                    it.setOrderStatus(OrderStatus.CANCELLED);
                    log.info("Order status is: {}", it.getOrderStatus());
                })
                .findAny()
                .orElseThrow(() -> new OrderNotFoundException("[Order not found]"));
        logService.dbLog(orderId, order);
        orderRepository.save(order);
        redisTemplate.opsForValue().setIfAbsent(
                CacheNames.ORDER_CACHE.getValue()
                        .concat(order.getId().toString()),
                order, CACHE_TTL, TimeUnit.MINUTES
        );
        log.info("[Order: #{} is cancelled]", orderId);
    }

    @Override
    @Transactional
    public Double calculateRatingOfItem(List<ItemEntity> items) {
        return items.stream()
                .map(ItemEntity::getRating)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        var cacheKey = CacheNames.ORDER_CACHE
                .getValue()
                .concat(id.toString());
        var cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (cacheValue != null) {
            logService.cacheLog(cacheKey, cacheValue);
            return orderObjectMapper.convertValue(cacheValue, OrderResponse.class);
        }
        OrderEntity orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(
                        MessageFormat.format("Order with id: {} not found", id)));
        OrderResponse orderResponse = orderMapper.mapToDto(orderEntity);
        orderCacheService.putInCache(orderResponse.getId().toString(), orderResponse);
        return orderResponse;
    }

    @Override
    public OrderMapResponse getOrderListOfUser(Long userId) {
        var cacheKey = CacheNames.USER_CACHE
                .getValue()
                .concat(userId.toString());
        Set<String> cacheKeys = redisTemplate.scan(
                        ScanOptions.scanOptions()
                                .match(REDIS_KEY_PREFIX)
                                .build())
                .stream()
                .filter(key -> key.startsWith(cacheKey))
                .collect(Collectors.toSet());
        var cacheValue = redisTemplate.opsForValue().multiGet(cacheKeys);
        log.info("Cache values by keys: {}", cacheValue);
        assert cacheValue != null;
        if (!cacheValue.isEmpty()){
            var orderResponses = cacheValue.stream()
                    .map(o->orderObjectMapper.convertValue(o, OrderResponse.class))
                    .filter(Objects::nonNull)
                    .toList();
            ListOrderResponse listOrderResponse = new ListOrderResponse();
            listOrderResponse.setOrders(orderResponses);
            OrderMapResponse orderMapResponse = new OrderMapResponse();
            Map<String, ListOrderResponse> map = new HashMap<>();
            map.put(cacheKey, listOrderResponse);
            orderMapResponse.setOrderMap(map);
            logService.cacheLog(cacheKey,cacheValue);
            return orderMapResponse;
        }


        var user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException(
                        MessageFormat.format("[User with id: {0} not found]",userId)
                ));
        var userItems = getItemsOfUserFromOrder(user)
                .stream()
                .toList();
        ListOrderResponse listOrderResponse = new ListOrderResponse();
        var userOrdersWithItems = getOrdersOfUser(user,userItems);
        log.info("User orders: {}",userOrdersWithItems);
        listOrderResponse.setOrders(userOrdersWithItems);
        userOrdersWithItems.forEach(u->{
            redisTemplate.opsForValue().setIfAbsent(
                    cacheKey,u,CACHE_TTL,TimeUnit.MINUTES
            );
        });
        OrderMapResponse orderMapResponse = new OrderMapResponse();
        Map<String,ListOrderResponse> map = new HashMap<>();
        map.put(cacheKey,listOrderResponse);
        orderMapResponse.setOrderMap(map);
        logService.dbLog(cacheKey,listOrderResponse);
        return orderMapResponse;

    }

    private Set<String> getCacheKeysOfUserWithPrefix(String userId){
        return redisTemplate.scan(
                        ScanOptions.scanOptions()
                                .match(REDIS_KEY_PREFIX)
                                .build())
                .stream()
                .filter(key -> key.startsWith(OrderServiceImpl.REDIS_KEY_PREFIX))
                .collect(Collectors.toSet());

    }


    private List<ItemEntity> getItemsOfUserFromOrder(UserEntity user) {
        var cacheKey = CacheNames.USER_CACHE
                .getValue()
                .concat(user.getId().toString());
        Set<String> cacheKeys = redisTemplate.scan(
                        ScanOptions.scanOptions()
                                .match(REDIS_KEY_PREFIX)
                                .build())
                .stream()
                .filter(key -> key.startsWith(CacheNames.USER_CACHE.getValue()
                        .concat(key)))
                .collect(Collectors.toSet());
        log.info("CacheKeys is: {}",cacheKeys);
        var value =  redisTemplate.opsForValue().multiGet(cacheKeys);
        if (!value.isEmpty() || value.stream()
                .anyMatch(Objects::isNull)){
            var userItems = value.stream()
                            .map(it->orderObjectMapper.convertValue(it,ItemResponse.class))
                                    .toList();


            logService.cacheLog(cacheKey,userItems);
            return itemMapper.mapToEntityList(userItems);
        }

        return itemRepository.findAllByIdIn(user.getOrders()
                .stream()
                .filter(Objects::nonNull)
                .map(OrderEntity::getItems)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .peek(itemEntity -> logService.dbLog(itemEntity.getId(),itemEntity))
                .map(ItemEntity::getId)
                .limit(MAX_LIMIT_OF_ORDERS_FOR_ONE_REQUEST)
                .toList());
    }

    private List<OrderResponse> getOrdersOfUser(UserEntity user, List<ItemEntity> items) {
        var userCacheKey = CacheNames.USER_CACHE
                .getValue()
                .concat(user.getId().toString());
        Set<String> cacheKeys = redisTemplate.scan(
                ScanOptions.scanOptions()
                        .match(REDIS_KEY_PREFIX)
                        .build())
                        .stream()
                        .filter(key->key.startsWith(userCacheKey))
                        .collect(Collectors.toSet());
        var cacheValues = redisTemplate.opsForValue().multiGet(cacheKeys);
        if (!cacheValues.isEmpty() || cacheValues.stream()
                .anyMatch(Objects::nonNull)){
            var orders = cacheValues.stream()
                    .map(order->orderObjectMapper.convertValue(order, UserEntity.class))
                    .map(UserEntity::getOrders)
                    .flatMap(Collection::stream)
                    .map(orderMapper::mapToDto)
                    .toList();
            logService.cacheLog(userCacheKey,orders);
            return orders;
        }

        Long orderId = items.stream()
                .map(ItemEntity::getOrder)
                .filter(Objects::nonNull)
                .map(OrderEntity::getId)
                .findAny()
                .orElseThrow(() -> new OrderNotFoundException(
                        MessageFormat.format(
                                "[Order for of user: {0} not found]", user.getId())));
        return orderMapper.mapToList(user.getOrders().stream()
                .filter(Objects::nonNull)
                .filter(orderEntity -> orderEntity.getId().equals(orderId))
                .peek(item->logService.dbLog(item.getId(),item))
                .toList());
    }

    @Override
    public ListOrderResponse findAllByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("[Ids can't be empty]");
        }
        var cacheKeys = ids.stream()
                .map(key->CacheNames.ORDER_CACHE
                        .getValue()
                        .concat(key.toString()))
                .toList();
        var cacheValues = redisTemplate.opsForValue().multiGet(cacheKeys);
        if (cacheValues!=null && cacheValues.stream()
                .allMatch(Objects::nonNull)){
            var values =  cacheValues.stream()
                    .map(value->orderObjectMapper.convertValue(value, OrderResponse.class))
                    .toList();
            if (!values.isEmpty()){
                logService.cacheLog();
                ListOrderResponse listOrderResponse = new ListOrderResponse();
                listOrderResponse.setOrders(values);
                return listOrderResponse;
            }
        }
        List<OrderResponse> orders = orderRepository.findAllById(ids)
                .stream()
                .map(orderMapper::mapToDto)
                .peek(order->redisTemplate.opsForValue()
                        .setIfAbsent(CacheNames.ORDER_CACHE
                                .getValue()
                                .concat(order.getId()
                                        .toString()),
                                               order,
                                           CACHE_TTL,
                                           TimeUnit.MINUTES
                ))
                .toList();
        ListOrderResponse listOrderResponse = new ListOrderResponse();
        listOrderResponse.setOrders(orders);
        logService.dbLog(orders);
        return listOrderResponse;
    }

    @Transactional
    @Override
    public void processOrder(OrderRequest orderRequest,
                              List<Long> itemsIds) {
        if (itemsIds.stream()
                .anyMatch(Objects::isNull) || itemsIds.isEmpty()){
            throw new IllegalArgumentException("[Ids can't be null or empty!]");
        }
        else if (itemRepository.findAllById(itemsIds).isEmpty()){
            throw new ItemNotFoundException("[Items not found!]");
        }
            BigDecimal totalAmount = BigDecimal.valueOf(0);
            var items = itemMapper.mapToItemRequestList(itemRepository.findAllById(itemsIds));
            for (ItemRequest itemRequest: items){
                BigDecimal itemQuantity = BigDecimal.valueOf(itemRequest.getQuantity());
                items.forEach(item->item.setQuantity(itemQuantity.intValue()));
                log.info("[Total amount: {}]",totalAmount);
            }

            if (totalAmount.compareTo(limit)>=0){
                orderRequest.setDiscount(discountValue);
            }
            else {
                orderRequest.setDiscount(BigDecimal.ZERO);
            }
            orderRequest.setTotalAmount(totalAmount.subtract(orderRequest.getDiscount()));
            OrderEntity orderEntity = orderMapper.mapToEntity(orderRequest,itemsIds);
            UserEntity user = userRepository.findById(orderRequest.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("[User not found]"));
            orderEntity.setUser(user);
            orderEntity.setOrderStatus(OrderStatus.PAID);
            orderEntity.setPayedAt(true);
            orderEntity.setIsShipped(items.stream()
                    .allMatch(ItemRequest::getIsShippable));
            List<ItemEntity> items_ = new ArrayList<>(itemRepository.findAllById(itemsIds));
            Set<String> itemsNamesList = items_.stream()
                    .sorted(Comparator.comparing(ItemEntity::getName)
                            .reversed())
                            .map(ItemEntity::getName)
                            .collect(Collectors.toSet());
            BigDecimal itemPrice = items_.stream()
                    .map(ItemEntity::getPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO,
                            BigDecimal::add);
            log.info("[Total item price: {}]",itemPrice);
            orderEntity.setTotalAmount(itemPrice);
            log.info("[Items name is: {}]",itemsNamesList);
            items_.forEach(itemEntity -> itemEntity.setOrder(orderEntity));
            orderEntity.setItems(items_);
            orderRepository.save(orderEntity);
            itemRepository.saveAll(items_);
            var cacheValue = orderMapper.mapToDto(orderEntity);
            redisTemplate.opsForValue()
                            .setIfAbsent(
                                    CacheNames.ORDER_CACHE
                                            .getValue()
                                            .concat(cacheValue.getId().toString()),
                                    cacheValue,
                                    CACHE_TTL,
                                    TimeUnit.MINUTES
                            );
            log.info("[Processed order status: {}, amount: {}, isShipped: {}]",
                    orderEntity.getOrderStatus(),
                    orderEntity.getTotalAmount(),
                    orderEntity.getIsShipped());
            Set<ItemEntity> itemList = calculateQuantityOfItems(
                    itemsIds,
                    orderRequest
            );
            publishEvent(orderEntity);

        }





        private Set<ItemEntity> calculateQuantityOfItems(List<Long> itemIds,
                                                          OrderRequest orderRequest){
            List<ItemEntity> items = itemRepository.findAllById(itemIds);
            List<ItemEntity> forUpdate = new ArrayList<>();
            List<ItemEntity> forDelete = new ArrayList<>();
            for (ItemEntity item:items){
                int quantity = item.getQuantity();
                int result = quantity-orderRequest.getQuantity();
                if (result<0){
                    throw new IllegalArgumentException("Количество товара меньше требуемого");
                }
                if (result==0){
                    forDelete.add(item);
                    itemRepository.delete(item);
                }
                if (!forDelete.isEmpty()){
                    itemRepository.deleteAllInBatch(forDelete);
                }
                else {
                    item.setQuantity(result);
                    forUpdate.add(item);
                }
                if (!forUpdate.isEmpty()){
                    itemRepository.saveAll(forUpdate);
                }
            }
            TreeSet<ItemEntity> itemEntityTreeSet = new TreeSet<>(
                    Comparator.comparing(ItemEntity::getName)
                            .thenComparing(ItemEntity::getPrice)
                            .thenComparing(ItemEntity::getQuantity));
            itemEntityTreeSet.addAll(forUpdate);
            log.info("Item set is: {}",itemEntityTreeSet);
            return itemEntityTreeSet;

        }





    @Override
    @EventListener
    public void handleOrderCreateEvent(OrderCreateEvent order) {
        var cacheKey = CacheNames.ORDER_CACHE
                .getValue()
                .concat(order.getOrderId()
                        .toString());
        var orderEntity = orderRepository.findById(order.getOrderId())
                        .map(orderMapper::mapToDto)
                        .orElseThrow(OrderNotFoundException::new);
        kafkaService.sendMessage(orderEntity, cacheKey);
        log.info("[Created order event is: {}]",order);
    }



    @Override
    @Transactional(readOnly = true)
    public ListOrderResponse findAllByStatus(OrderStatus status,
                                             int pageNumber,
                                             int pageSize) {
        Set<String> redisKeys = redisTemplate.scan(
                ScanOptions.scanOptions()
                        .match(REDIS_KEY_PREFIX)
                        .count(1000)
                        .build())
                .stream()
                .filter(key->key.startsWith(CacheNames.ORDER_CACHE.getValue()))
                .collect(Collectors.toSet());
        var cacheValues = redisTemplate.opsForValue().multiGet(redisKeys);
        if (cacheValues != null && cacheValues.stream()
                .anyMatch(Objects::nonNull)) {
            logService.cacheLog(redisKeys,cacheValues);
            var valueList = cacheValues.stream()
                    .map(value->orderObjectMapper.convertValue(value, OrderResponse.class))
                    .toList();
            ListOrderResponse listOrderResponse = new ListOrderResponse();
            listOrderResponse.setOrders(valueList);
            return listOrderResponse;
        }
        Pageable pages = PageRequest.of(pageNumber, pageSize);
        List<OrderResponse> orderResponses = orderRepository.findAllByOrderStatus(status, pages)
                .stream()
                .map(orderMapper::mapToDto)
                .peek(order->redisTemplate.opsForValue()
                        .setIfAbsent(CacheNames.ORDER_CACHE
                                .getValue()
                                .concat(order.getId().toString()),
                                order,
                                CACHE_TTL,
                                TimeUnit.MINUTES))
                .toList();
        ListOrderResponse response = new ListOrderResponse();
        response.setOrders(orderResponses);
        logService.dbLog(response.getOrders());
        return response;
    }




    private void publishEvent(OrderEntity order) {
        var itemNames = order.getItems()
                .stream()
                .map(ItemEntity::getName)
                .toList();
        eventPublisher.publishEvent(
                    new OrderCreateEvent(this,
                        order.getId(),
                        order.getPayedAt(),
                        order.getOrderStatus().name(),
                        order.getTotalAmount(),
                        order.getIsShipped(),
                        itemNames));
        }


}




