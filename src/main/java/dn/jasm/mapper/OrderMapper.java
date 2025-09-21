package dn.jasm.mapper;

import dn.jasm.dto.order.ListOrderResponse;
import dn.jasm.dto.order.OrderRequest;
import dn.jasm.dto.order.OrderResponse;
import dn.jasm.entity.ItemEntity;
import dn.jasm.entity.OrderEntity;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.repository.ItemRepository;
import dn.jasm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final ItemMapper itemMapper;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;



    public OrderEntity mapToEntity(OrderRequest orderRequest,List<Long> itemsIds){
        OrderEntity order = new OrderEntity();
        order.setAmount(orderRequest.getTotalAmount());
        var items = itemRepository.findAllByIdIn(itemsIds);
        order.setItems(items);
        order.setDiscount(orderRequest.getDiscount());
        return order;
    }

    public OrderEntity mapToEntity(OrderResponse orderResponse){
        OrderEntity order = new OrderEntity();
        order.setId(orderResponse.getId());
        order.setAmount(orderResponse.getTotalAmount());
        order.setUser(userRepository.findById(orderResponse.getId())
                .orElseThrow(UserNotFoundException::new));
        order.setItems(orderResponse.getItems());
        order.setQuantityOfItems(orderResponse.getQuantity());
        order.setPayedAt(orderResponse.getIsPayed());
        order.setIsShipped(orderResponse.getIsShipped());
        return order;
    }

    public OrderResponse mapToDto(OrderEntity order){
        return OrderResponse.builder()
                .id(order.getId())
                .isShipped(true)
                .quantity(order.getItems()
                        .stream()
                        .toList()
                        .size())
                .userId(order.getUser().getId())
                .totalAmount(order.getAmount())
                .isPayed(true)
                .items(order.getItems())
                .build();
    }

    public ListOrderResponse mapToDtoList(List<OrderEntity> orders){
        ListOrderResponse listOrderResponse = new ListOrderResponse();
        listOrderResponse.setOrders(orders.stream()
                .filter(Objects::nonNull)
                .filter(OrderEntity::getPayedAt)
                .map(this::mapToDto)
                .toList());
        return listOrderResponse;
    }

    public List<OrderEntity> mapToEntityList(List<OrderResponse> orders){
        return orders.stream()
                .map(this::mapToEntity)
                .toList();
    }

    public List<OrderResponse> mapToList(List<OrderEntity> orders){
        return orders.stream()
                .filter(Objects::nonNull)
                .map(this::mapToDto)
                .toList();
    }


}
