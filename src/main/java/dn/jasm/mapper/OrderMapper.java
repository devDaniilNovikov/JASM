package dn.jasm.mapper;

import dn.jasm.dto.order.ListOrderResponse;
import dn.jasm.entity.OrderEntity;
import dn.jasm.dto.order.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import java.util.*;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper extends Mappable<OrderEntity, OrderResponse> {

    @Override
    @Mappings({
            @Mapping(target = "totalAmount", source = "amount"),
            @Mapping(target = "userId", source = "user.id"),
            @Mapping(target = "quantity", expression = "java(order.getItems().size())")
    })
    OrderResponse toDto(OrderEntity order);

//    @Override
//    @Mappings({
//            @Mapping(target = "totalAmount", source = "amount"),
//            @Mapping(target = "userId", source = "user.id"),
//            @Mapping(target = "quantity", expression = "java(order.getItems().size())")
//    })
//    OrderEntity toEntity(OrderEntity order);
//
//    default ListOrderResponse toList(List<OrderEntity> order){
//        ListOrderResponse listOrderResponse = new ListOrderResponse();
//        listOrderResponse.setOrders(order.stream()
//                .map(this::toDto)
//                .toList());
//        return listOrderResponse;
//    }
}
