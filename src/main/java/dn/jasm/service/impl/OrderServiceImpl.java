package dn.jasm.service.impl;


import com.stripe.model.Price;
import dn.jasm.configuration.aop.Loggable;
import dn.jasm.configuration.aop.TimeResulting;
import dn.jasm.configuration.redis.RedisService;
import dn.jasm.dto.order.ListOrderResponse;
import dn.jasm.dto.order.OrderResponse;
import dn.jasm.entity.OrderEntity;
import dn.jasm.exception.OrderNotFoundException;
import dn.jasm.exception.RedisKeyException;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.entity.ItemEntity;
import dn.jasm.mapper.ItemMapper;
import dn.jasm.mapper.OrderMapper;
import dn.jasm.entity.enums.OrderStatus;
import dn.jasm.mapper.UserMapper;
import dn.jasm.repository.ItemRepository;
import dn.jasm.repository.OrderRepository;
import dn.jasm.entity.UserEntity;
import dn.jasm.repository.UserRepository;
import dn.jasm.service.ItemService;
import dn.jasm.service.OrderService;
import dn.jasm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final UserService userService;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final RedisService redisService;

    private final Map<String, ListOrderResponse> orderWithUsernameOfOwner = new HashMap<>();

    @Override
    @Transactional
    public OrderResponse createOrder(Price price, List<ItemEntity> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Items can't be null");
        }
        OrderEntity order = new OrderEntity();
        var itemList = items.stream()
                .filter(it -> price.getUnitAmountDecimal().equals(it.getPrice()))
                .toList();
        itemList.forEach(item -> item.setOrder(order));
        order.setItems(itemList);
        order.setAmount(price.getUnitAmountDecimal());
        order.setPayedAt(true);
        order.setCreatedAt(LocalDateTime.now());
        order.setRating(0.0);
        orderRepository.save(order);
        itemRepository.saveAll(itemList);
        log.info("Created order: {}", order.getId());
        return orderMapper.toDto(order);

    }

    @Override
    public BigDecimal calculateTotalAmountOfOrder(List<ItemEntity> items) {
        return items.stream()
                .map(ItemEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId, Long userId) {
        Thread.startVirtualThread(()-> {
            var order = orderRepository.findById(orderId)
                    .stream()
                    .map(o -> {
                        BigDecimal orderTotalAmount = calculateTotalAmountOfOrder(o.getItems());
                        o.setAmount(orderTotalAmount);
                        o.setPayedAt(true);
                        o.setCreatedAt(LocalDateTime.now());
                        o.setOrderStatus(OrderStatus.PAID);
                        var userWithChangedBalance = userRepository.findById(userId)
                                .stream()
                                .peek(user -> {
                                    var totalBalance = user.getBalance().subtract(orderTotalAmount);
                                    user.setBalance(totalBalance);
                                })
                                .findAny()
                                .orElseThrow(() -> new UserNotFoundException(
                                        MessageFormat.format("User with id: {0} not found", userId)));
                        userRepository.save(userWithChangedBalance);
                        o.setUser(userWithChangedBalance);
                        userWithChangedBalance.getOrders().add(o);
                        return orderRepository.save(o);
                    })
                    .findAny()
                    .orElseThrow(RuntimeException::new);
            log.info("Order with id #{} completed", order.getId());
        });

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
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order: #{} is cancelled", orderId);
    }

    @Override
    public Double calculateRatingOfItem(List<ItemEntity> items) {
        return items.stream()
                .map(ItemEntity::getRating)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        return orderMapper.toDto(orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(
                        MessageFormat.format("Order with id: {0} not found", id))));
    }

    @Override
    public Map<String, ListOrderResponse> getOrderListOfUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        MessageFormat.format("User with ID: {0} not found", userId)));
        List<ItemEntity> items = getItemsOfUserFromOrder(user);
        ListOrderResponse listOrderResponse = new ListOrderResponse();
        List<OrderEntity> orders = getOrdersOfUser(user,items);
        listOrderResponse.setOrders(orders);
        String username = user.getUsername();
        orderWithUsernameOfOwner.put(username,listOrderResponse);
        ListOrderResponse cacheValue = orderWithUsernameOfOwner.get(username);
        try {
            redisService.writeObjectInRedis(username, cacheValue);
        }catch (RedisKeyException e){
            log.error("This key already put in redis: {}",username);
        }
        return orderWithUsernameOfOwner;
    }


    private List<ItemEntity> getItemsOfUserFromOrder(UserEntity user){
        return itemRepository.findAllByIdIn(user.getOrders()
                .stream()
                .filter(Objects::nonNull)
                .map(OrderEntity::getItems)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(ItemEntity::getId)
                .limit(5)
                .toList());
    }

    private List<OrderEntity> getOrdersOfUser(UserEntity user,List<ItemEntity> items){
        Long orderId = items.stream()
                .map(ItemEntity::getOrder)
                .filter(Objects::nonNull)
                .map(OrderEntity::getId)
                .findAny()
                .orElseThrow(()->new OrderNotFoundException(
                        MessageFormat.format(
                                "Order for of user: {0} not found",user.getId())));
        return user.getOrders().stream()
                .filter(Objects::nonNull)
                .filter(orderEntity -> orderEntity.getId().equals(orderId))
                .toList();
    }

    @Override
    public List<OrderResponse> findAllByIds(List<Long> ids) {
        if (ids.isEmpty()){
            throw new IllegalArgumentException("Ids can't be empty");
        }
        return orderMapper.toDtoList(orderRepository.findAllById(ids));
    }
}



