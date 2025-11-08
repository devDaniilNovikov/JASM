package dn.jasm.client;


import dn.jasm.dto.item.ItemRequest;
import dn.jasm.dto.order.ListOrderResponse;
import dn.jasm.dto.order.OrderMapResponse;
import dn.jasm.dto.order.OrderResponse;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

@Component
@HttpExchange("http://localhost:3000/api/v1/orders")
public interface OrderClient {

    @PostExchange("/process")
    @Retryable(maxAttempts = 4)
    void processOrder(@RequestParam List<Long> ids,
                      @RequestBody ItemRequest itemRequest);

    @GetExchange("/list")
    @Retryable(maxAttempts = 5)
    ListOrderResponse findAll(@RequestParam(defaultValue = "10") int pageSize,
                              @RequestParam(defaultValue = "0") int pageNumber);

    @GetExchange()
    @Retryable(maxAttempts = 5)
    OrderMapResponse getOrderListOfUser(@RequestParam Long userId);

    @GetExchange("/{id}")
    @Retryable(maxAttempts = 5)
    OrderResponse getOrderById(@PathVariable Long id);

}
