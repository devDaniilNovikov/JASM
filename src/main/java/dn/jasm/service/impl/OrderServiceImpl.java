package dn.jasm.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import dn.jasm.configuration.redis.CacheNames;
import dn.jasm.dto.item.ItemRequest;
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
import dn.jasm.service.OrderService;
import dn.jasm.service.cache.OrderCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Value("${order.discount.value}")
    private BigDecimal discountValue;

    @Value("${order.limit.value}")
    private BigDecimal limit;

    private static final Long MAX_LIMIT_OF_ORDERS_FOR_ONE_REQUEST = 5L;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderMapper orderMapper;
    private final OrderCacheService orderCacheService;
    private final ObjectMapper orderObjectMapper;
    private final RedisTemplate<String,Object> redisTemplate;
    private final TransactionTemplate transactionTemplate;
    private final KafkaService kafkaService;
    ExecutorService executorService = Executors.newFixedThreadPool(100);



    private final Map<String,ListOrderResponse> orderWithUsernameOfOwner = new HashMap<>();


    @Override
    public BigDecimal calculateTotalAmountOfOrder(List<ItemEntity> items) {
        return items.stream()
                .map(ItemEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public ListOrderResponse findAll(int pageSize, int pageNumber) {
        PageRequest pageRequest = PageRequest.of(pageSize, pageNumber);
        ListOrderResponse listOrderResponse = orderMapper.mapToDtoList(
                orderRepository.findAll(pageRequest)
                        .getContent());
        var id = UUID.randomUUID().toString();
        orderCacheService.putInCache(id,listOrderResponse.toString());
        return listOrderResponse;
    }


    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        var order = userRepository.findById(userId)
                .stream()
                .map(UserEntity::getOrders)
                .flatMap(Collection::stream)
                .filter(o -> o.getId().equals(orderId))
                .findAny()
                .orElseThrow(() -> new OrderNotFoundException("[Order not found]"));
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
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
        OrderResponse cache = orderCacheService.getOrderFromCache(String.valueOf(id));
        if (cache != null) {
            return orderObjectMapper.convertValue(cache, OrderResponse.class);
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
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        MessageFormat.format("[User with ID: {0} not found]", userId)));
        List<ItemEntity> items = getItemsOfUserFromOrder(user);
        ListOrderResponse listOrderResponse = new ListOrderResponse();
        listOrderResponse.setOrders(getOrdersOfUser(user,items));
        String username = user.getUsername();
        orderWithUsernameOfOwner.put(username, listOrderResponse);
        ListOrderResponse cacheValue = orderWithUsernameOfOwner.get(username);
        OrderMapResponse orderMapResponse = new OrderMapResponse();
        orderMapResponse.setOrderMap(orderWithUsernameOfOwner);
        if (orderCacheService.getOrderFromCache(username)==null){
            orderCacheService.putInCache(username,cacheValue);
            return orderMapResponse;
        }
        else {
            return orderObjectMapper.convertValue(orderMapResponse, OrderMapResponse.class);
        }
    }


    private List<ItemEntity> getItemsOfUserFromOrder(UserEntity user) {
        return itemRepository.findAllByIdIn(user.getOrders()
                .stream()
                .filter(Objects::nonNull)
                .map(OrderEntity::getItems)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(ItemEntity::getId)
                .limit(MAX_LIMIT_OF_ORDERS_FOR_ONE_REQUEST)
                .toList());
    }

    private List<OrderResponse> getOrdersOfUser(UserEntity user, List<ItemEntity> items) {
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
                .toList());
    }

    @Override
    public ListOrderResponse findAllByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("[Ids can't be empty]");
        }
        return orderMapper.mapToDtoList(orderRepository.findAllById(ids)
                .stream()
                .filter(order->order.getOrderStatus().equals(OrderStatus.NEW))
                .peek(order->orderCacheService.putInCache(String.valueOf(order.getId()),order))
                .toList());

    }

    @Transactional
    @Override
    public void processOrder(OrderRequest orderRequest,
                              List<Long> itemsIds) {
        if (itemsIds.stream()
                .anyMatch(Objects::isNull)|| itemsIds.isEmpty()){
            throw new IllegalArgumentException("[Ids can't be null or empty!]");
        }
        else if (itemRepository.findAllById(itemsIds).isEmpty()){
            throw new ItemNotFoundException("[Items not found!]");
        }
            BigDecimal totalAmount = BigDecimal.valueOf(0);
            var items = itemMapper.mapToItemRequestList(itemRepository.findAllById(itemsIds));
            for (ItemRequest itemRequest: items){
                BigDecimal itemQuantity = BigDecimal.valueOf(itemRequest.getQuantity());
                totalAmount = totalAmount.add(itemRequest.getPrice().multiply(itemQuantity));
                log.info("[Total amount: {}]",totalAmount);
            }

            if (totalAmount.compareTo(limit)>0){
                orderRequest.setDiscount(discountValue);
            }
            else {
                orderRequest.setDiscount(BigDecimal.ZERO);
            }
            orderRequest.setTotalAmount(totalAmount.subtract(orderRequest.getDiscount()));
            var orderEntity = orderMapper.mapToEntity(orderRequest,itemsIds);
            var user = userRepository.findById(orderRequest.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("[User not found]"));
            orderEntity.setUser(user);
            orderEntity.setOrderStatus(OrderStatus.PAID);
            orderEntity.setPayedAt(true);
            orderEntity.setIsShipped(items.stream()
                    .allMatch(ItemRequest::getIsShippable));
            var items_ = new ArrayList<>(itemRepository.findAllById(itemsIds));
            var itemsIdsList = items_.stream()
                            .map(ItemEntity::getName)
                            .toList();
            log.info("Items name is: {}",itemsIdsList);
            items_.forEach(itemEntity -> itemEntity.setOrder(orderEntity));
            orderEntity.setItems(items_);
            orderRepository.save(orderEntity);
            itemRepository.saveAll(items_);
            log.info("[Processed order status: {}, amount: {}, isShipped: {}]",
                    orderEntity.getOrderStatus(),
                    orderEntity.getAmount(),
                    orderEntity.getIsShipped());
            Set<ItemEntity> itemList = calculateQuantityOfItems(
                    itemsIds,
                    orderRequest
            );
            var orderDto = orderMapper.mapToDto(orderEntity);
            var cacheKey = CacheNames.ORDER_CACHE
                    .getValue()
                    .concat(orderDto.getId()
                            .toString());
            kafkaService.sendMessage(orderDto, cacheKey);
            log.info("[Item list is: {}]",itemList
                    .stream()
                    .map(ItemEntity::getName)
                    .toList());
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
            return itemEntityTreeSet;

        }

        private BigDecimal calculateTotalAmount(List<Long> itemIds){
            BigDecimal totalAmount = BigDecimal.valueOf(0);
            var items = itemMapper.mapToItemRequestList(itemRepository.findAllById(itemIds));
            for (ItemRequest item: items){
                BigDecimal itemQuantity = BigDecimal.valueOf(item.getQuantity());
                totalAmount = totalAmount.add(item.getPrice().multiply(itemQuantity));
                log.info("[Total amount: {}]",totalAmount);
            }
            return totalAmount;
        }





    @Override
    @EventListener
    public void handleOrderCreateEvent(OrderEntity order) {
        var orderStringValue = new OrderCreateEvent(this,
                order.getId(),
                order.getPayedAt(),
                order.getOrderStatus().name(),
                order.getAmount(),
                order.getIsShipped(),
                order.getItems()
                        .stream()
                        .map(ItemEntity::getName)
                        .toList());
//        redisTemplate.opsForValue()
//                        .setIfAbsent(
//                                orderStringValue.getOrderId().toString(),
//                                orderStringValue.toString(),
//                                Duration.ofMinutes(10)
//                        );
        log.info("[Created order event is: {}]",order);
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
                        order.getAmount(),
                        order.getIsShipped(),
                        itemNames));
        }


}




