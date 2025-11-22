package dn.jasm.controller;

import com.stripe.model.PaymentIntent;
import dn.jasm.dto.order.ListOrderResponse;
import dn.jasm.dto.order.OrderMapResponse;
import dn.jasm.dto.order.OrderRequest;
import dn.jasm.dto.order.OrderResponse;
import dn.jasm.entity.enums.OrderStatus;
import dn.jasm.service.OrderService;
import dn.jasm.service.cache.OrderCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private static final String GET_ORDER_LIST = "/api/v1/orders";
    private static final String PROCESS_ORDER = "/api/v1/orders/process";
    private static final String GET_ORDER_BY_ID = "/api/v1/orders/{id}";
    private static final String GET_ORDERS_LIST = "/api/v1/orders/list";
    private static final String GET_ORDERS_LIST_BY_STATUS = "/api/v1/orders/by-status";


    private final OrderService orderService;

    @GetMapping(GET_ORDER_LIST)
    @ResponseStatus(HttpStatus.OK)
    public OrderMapResponse getOrderListOfUser(@RequestParam Long userId){
        return orderService.getOrderListOfUser(userId);
    }

     @GetMapping(GET_ORDERS_LIST)
     @ResponseStatus(HttpStatus.OK)
     public ListOrderResponse findAll(@RequestParam(defaultValue = "10") int pageSize,
                                      @RequestParam(defaultValue = "0") int pageNumber){
        return orderService.findAll(pageSize, pageNumber);

     }


    @PostMapping(PROCESS_ORDER)
    @ResponseStatus(HttpStatus.CREATED)
    public void processOrder(@RequestBody OrderRequest orderRequest,
                             @RequestParam List<Long> itemsIds){
        orderService.processOrder(orderRequest,itemsIds);
    }

    @GetMapping(GET_ORDER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse getOrderById(@PathVariable Long id){
        return orderService.getOrderById(id);
    }

    @GetMapping(GET_ORDERS_LIST_BY_STATUS)
    public ListOrderResponse findAllByStatus(OrderStatus status,
                                             int pageNumber,
                                             int pageSize){
        return orderService.findAllByStatus(
                status,pageNumber,pageSize
        );
    }




}
