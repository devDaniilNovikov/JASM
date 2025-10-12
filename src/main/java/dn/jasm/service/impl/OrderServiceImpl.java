package dn.jasm.service.impl;


import com.stripe.model.Price;
import dn.jasm.dto.item.ItemRequest;
import dn.jasm.dto.order.OrderMapResponse;
import dn.jasm.dto.order.OrderRequest;
import dn.jasm.event.CardCreateEvent;
import dn.jasm.event.OrderCreateEvent;
import dn.jasm.exception.ItemNotFoundException;
import dn.jasm.mapper.ItemMapper;
import dn.jasm.mapper.OrderMapper;
import dn.jasm.service.RedisService;
import dn.jasm.dto.order.ListOrderResponse;
import dn.jasm.dto.order.OrderResponse;
import dn.jasm.entity.OrderEntity;
import dn.jasm.exception.OrderNotFoundException;
import dn.jasm.exception.RedisKeyException;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.entity.ItemEntity;
import dn.jasm.entity.enums.OrderStatus;
import dn.jasm.repository.ItemRepository;
import dn.jasm.repository.OrderRepository;
import dn.jasm.entity.UserEntity;
import dn.jasm.repository.UserRepository;
import dn.jasm.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Value("${order.discount.value}")
    private BigDecimal discountValue;

    @Value("${order.limit.value}")
    private BigDecimal limit;

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderMapper orderMapper;
    private final RedisService redisService;

    private final Map<String,ListOrderResponse> orderWithUsernameOfOwner = new HashMap<>();

    @Override
    @Transactional
    public OrderResponse createOrder(Price price, List<ItemEntity> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("[Items can't be null]");
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
        log.info("[Created order: {}]", order.getId());
        return orderMapper.mapToDto(order);

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
        Thread.startVirtualThread(() -> {
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
                                        MessageFormat.format("[User with id: {0} not found]", userId)));
                        userRepository.save(userWithChangedBalance);
                        o.setUser(userWithChangedBalance);
                        userWithChangedBalance.getOrders().add(o);
                        return orderRepository.save(o);
                    })
                    .findAny()
                    .orElseThrow(RuntimeException::new);
            log.info("[Order with id #{} completed]", order.getId());
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
        return orderMapper.mapToDto(orderRepository.findById(id)
                .stream()
                .peek(order->redisService.writeObjectInRedis(order.getId().toString(),order))
                .findAny()
                .orElseThrow(() -> new OrderNotFoundException(
                        MessageFormat.format("[Order with id: {0} not found]", id))));
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
        try {
            redisService.writeObjectInRedis(username, cacheValue);
        } catch (RedisKeyException e) {
            log.error("[This key already put in redis: {}]", username);
        }
        return orderMapResponse;
    }


    private List<ItemEntity> getItemsOfUserFromOrder(UserEntity user) {
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
                .peek(order->redisService.writeObjectInRedis(String.valueOf(order.getId()),order))
                .toList());

    }

    @Transactional
    @Override
    public void processOrder(OrderRequest orderRequest,List<Long> itemsIds) {
        if (itemsIds == null || itemsIds.isEmpty()){
            throw new IllegalArgumentException("[Ids can't be null or empty!]");
        }
        else if (itemRepository.findAllById(itemsIds).isEmpty()){
            throw new ItemNotFoundException("[Items not found!]");
        }
            BigDecimal totalAmount = BigDecimal.valueOf(0);
            var items = itemMapper.mapToItemRequestList(itemRepository.findAllByIdIn(itemsIds));
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
            orderEntity.setOrderStatus(OrderStatus.PAID);
            orderEntity.setPayedAt(true);
            orderEntity.setIsShipped(items
                    .stream()
                            .map(ItemRequest::getIsShippable)
                    .reduce(true, (t, f)-> true));
            log.info("[Processed order status: {}, amount: {}, isShipped: {}]",
                    orderEntity.getOrderStatus(),
                    orderEntity.getAmount(),
                    orderEntity.getIsShipped());
            orderRepository.save(orderEntity);
            itemRepository.deleteAllByIdInBatch(itemsIds);
            publishEvent(orderEntity);
        }

    @Override
    @EventListener
    public void handleOrderCreateEvent(OrderCreateEvent orderCreateEvent) {
        redisService.writeObjectInRedis(String.valueOf(orderCreateEvent.getOrderId()),
                orderCreateEvent.getStatus());
        log.info("[Created order event is: {}]",orderCreateEvent);
    }


    private void publishEvent(OrderEntity order) {
        var itemIds = order.getItems().stream()
                .map(ItemEntity::getId)
                .toList();
        eventPublisher.publishEvent(
                    new OrderCreateEvent(this,
                        order.getId(),
                        order.getPayedAt(),
                        order.getOrderStatus().name(),
                        order.getAmount(),
                        order.getIsShipped(),
                        itemIds));
        }


}




