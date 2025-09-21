package dn.jasm.controller;

import dn.jasm.dto.order.ListOrderResponse;
import dn.jasm.dto.order.OrderMapResponse;
import dn.jasm.dto.order.OrderRequest;
import dn.jasm.dto.order.OrderResponse;
import dn.jasm.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private static final String GET_ORDER_LIST = "/api/v1/orders";
    private static final String PROCESS_ORDER = "/api/v1/orders/process";


    private final OrderService orderService;

    @GetMapping(GET_ORDER_LIST)
    @ResponseStatus(HttpStatus.OK)
    public OrderMapResponse getOrderListOfUser(@RequestParam Long userId){
        return orderService.getOrderListOfUser(userId);
    }


    @PostMapping(PROCESS_ORDER)
    public void processOrder(@RequestBody OrderRequest orderRequest,
                             @RequestParam List<Long> itemsIds){
        orderService.processOrder(orderRequest,itemsIds);
    }


}
