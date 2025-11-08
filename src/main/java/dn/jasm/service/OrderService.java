package dn.jasm.service;

import com.stripe.model.Price;
import dn.jasm.dto.order.ListOrderResponse;
import dn.jasm.dto.order.OrderMapResponse;
import dn.jasm.dto.order.OrderRequest;
import dn.jasm.dto.order.OrderResponse;
import dn.jasm.entity.ItemEntity;
import dn.jasm.entity.OrderEntity;
import dn.jasm.event.OrderCreateEvent;

import java.math.BigDecimal;
import java.util.*;

public interface OrderService {

    OrderResponse createOrder(Price price, List<ItemEntity> items);

    BigDecimal calculateTotalAmountOfOrder(List<ItemEntity> items);

    ListOrderResponse findAll(int pageSize,int pageNumber);


    void cancelOrder(Long orderId,Long userId);

    Double calculateRatingOfItem(List<ItemEntity> items);

    OrderResponse getOrderById(Long id);

    OrderMapResponse getOrderListOfUser(Long userId);

    ListOrderResponse findAllByIds(List<Long> ids);

    void processOrder(OrderRequest orderRequest,List<Long> itemsIds);

    void handleOrderCreateEvent(OrderCreateEvent orderCreateEvent);








}
