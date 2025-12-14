package dn.jasm.controller;

import com.stripe.model.PaymentIntent;
import dn.jasm.configuration.swagger.order.SwaggerAnnotationForOrder;
import dn.jasm.configuration.swagger.order.SwaggerAnnotationForOrderCollection;
import dn.jasm.dto.order.ListOrderResponse;
import dn.jasm.dto.order.OrderMapResponse;
import dn.jasm.dto.order.OrderRequest;
import dn.jasm.dto.order.OrderResponse;
import dn.jasm.entity.enums.OrderStatus;
import dn.jasm.service.OrderService;
import dn.jasm.service.cache.OrderCacheService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Tag(name = "Order" ,description = "Действия с заказами")
public class OrderController {

    private static final String GET_ORDER_LIST = "/api/v1/orders";
    private static final String PROCESS_ORDER = "/api/v1/orders/process";
    private static final String GET_SINGLE_ORDER_BY_ID = "/api/v1/orders/{id}";
    private static final String GET_ORDERS_LIST = "/api/v1/orders/list";
    private static final String GET_ORDERS_LIST_BY_STATUS = "/api/v1/orders/by-status";
    private static final String CANCEL_ORDER = "/api/v1/order/cancel";
    private static final String GET_MULTIPLE_ORDERS_BY_IDS = "/api/v1/orders/by-ids";
    private static final String PAGE_SIZE_DEFAULT_VALUE = "10";
    private static final String PAGE_NUMBER_DEFAULT_VALUE = "0";

    private final OrderService orderService;

    @PostMapping(CANCEL_ORDER)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForOrder(operation = "Отмена заказа")
    public void cancelOrder(@RequestParam Long orderId,
                            @RequestParam Long userId){
        orderService.cancelOrder(orderId,userId);
    }

    @GetMapping(GET_ORDER_LIST)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForOrderCollection(operation = "Получение списка заказов пользователя")
    public OrderMapResponse getOrderListOfUser(@RequestParam Long userId){
        return orderService.getOrderListOfUser(userId);
    }

     @GetMapping(GET_ORDERS_LIST)
     @ResponseStatus(HttpStatus.OK)
     @SwaggerAnnotationForOrderCollection(operation = "Получение списка заказов с пагинацией")
     public ListOrderResponse findAll(@RequestParam(defaultValue = PAGE_SIZE_DEFAULT_VALUE) int pageSize,
                                      @RequestParam(defaultValue = PAGE_NUMBER_DEFAULT_VALUE) int pageNumber){
        return orderService.findAll(pageSize, pageNumber);

     }

     @GetMapping(GET_MULTIPLE_ORDERS_BY_IDS)
     @SwaggerAnnotationForOrderCollection(operation = "Получение списка нескольких заказов")
     @ResponseStatus(HttpStatus.OK)
     public ListOrderResponse findAllByIds(@RequestParam List<Long> ids){
        return orderService.findAllByIds(ids);
    }


    @PostMapping(PROCESS_ORDER)
    @ResponseStatus(HttpStatus.CREATED)
    @SwaggerAnnotationForOrder(operation = "Создание заказа")
    public void processOrder(@RequestBody OrderRequest orderRequest,
                             @RequestParam List<Long> itemsIds){
        orderService.processOrder(orderRequest,itemsIds);
    }

    @GetMapping(GET_SINGLE_ORDER_BY_ID)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForOrder(operation = "Получение заказа")
    public OrderResponse getOrderById(@PathVariable Long id){
        return orderService.getOrderById(id);
    }

    @GetMapping(GET_ORDERS_LIST_BY_STATUS)
    @ResponseStatus(HttpStatus.OK)
    @SwaggerAnnotationForOrderCollection(operation = "Получение списка заказов по их статусам  с пагинацией")
    public ListOrderResponse findAllByStatus(OrderStatus status,
                                             int pageNumber,
                                             int pageSize){
        return orderService.findAllByStatus(
                status,pageNumber,pageSize
        );
    }




}
