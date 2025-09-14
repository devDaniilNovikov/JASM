package dn.jasm.controller;

import dn.jasm.dto.order.ListOrderResponse;
import dn.jasm.dto.order.OrderMapResponse;
import dn.jasm.dto.order.OrderResponse;
import dn.jasm.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private static final String GET_ORDER_LIST = "/api/v1/orders";


    private final OrderService orderService;

    @GetMapping(GET_ORDER_LIST)
    public OrderMapResponse getOrderListOfUser(@RequestParam Long userId){
        return orderService.getOrderListOfUser(userId);
    }
}
