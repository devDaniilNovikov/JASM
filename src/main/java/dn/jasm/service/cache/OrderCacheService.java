package dn.jasm.service.cache;

import dn.jasm.dto.order.OrderResponse;
import org.springframework.web.service.annotation.HttpExchange;

public interface OrderCacheService {


    @HttpExchange
    void putInCache(String id,
                    Object value);

    OrderResponse getOrderFromCache(String id);



}
