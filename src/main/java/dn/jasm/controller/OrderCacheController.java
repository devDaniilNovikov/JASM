package dn.jasm.controller;

import dn.jasm.dto.order.ListOrderResponse;
import dn.jasm.service.cache.OrderCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequiredArgsConstructor
public class OrderCacheController {

    private static final String DELETE_ORDER_CACHES = "/api/v1/orders/cache/delete";
    private static final String GET_ORDER_CACHE_LIST = "/api/v1/orders/cache/list";


    private final OrderCacheService orderCacheService;


    @DeleteMapping(DELETE_ORDER_CACHES)
    public void deleteCachesByKeys(@RequestParam Set<String> ids){
        orderCacheService.deleteFromCache(ids);
    }

    @GetMapping(GET_ORDER_CACHE_LIST)
    public ListOrderResponse getOrderListFromCache(@RequestParam Set<String> ids){
        return orderCacheService.getOrderListFromCache(ids);
    }
}
