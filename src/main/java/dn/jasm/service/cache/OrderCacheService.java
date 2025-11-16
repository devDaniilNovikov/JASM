package dn.jasm.service.cache;

import dn.jasm.dto.order.ListOrderResponse;
import dn.jasm.dto.order.OrderResponse;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.Set;

public interface OrderCacheService {


    void putInCache(String id,
                    Object value);

    OrderResponse getOrderFromCache(String id);

    ListOrderResponse getOrderListFromCache(Set<String> ids);

    void deleteFromCache(String id);

    void deleteFromCache(Set<String> keys);



}
