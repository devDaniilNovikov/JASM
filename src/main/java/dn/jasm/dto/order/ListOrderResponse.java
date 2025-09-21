package dn.jasm.dto.order;

import dn.jasm.entity.ItemEntity;
import dn.jasm.entity.OrderEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.*;

@Data
public class ListOrderResponse {
    private List<OrderResponse> orders = new ArrayList<>();
}
