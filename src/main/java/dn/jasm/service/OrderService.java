package dn.jasm.service;

import com.stripe.model.Price;
import dn.jasm.dto.order.ListOrderResponse;
import dn.jasm.dto.order.OrderResponse;
import dn.jasm.entity.ItemEntity;
import dn.jasm.entity.OrderEntity;

import java.math.BigDecimal;
import java.util.*;

public interface OrderService {

    OrderResponse createOrder(Price price, List<ItemEntity> items);

    BigDecimal calculateTotalAmountOfOrder(List<ItemEntity> items);

    void completeOrder(Long orderId,Long userId);

    void cancelOrder(Long orderId,Long userId);

    Double calculateRatingOfItem(List<ItemEntity> items);

    OrderResponse getOrderById(Long id);

    Map<String, ListOrderResponse> getOrderListOfUser(Long userId);

    List<OrderResponse> findAllByIds(List<Long> ids);








}
