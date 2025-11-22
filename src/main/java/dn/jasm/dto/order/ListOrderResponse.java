package dn.jasm.dto.order;

import dn.jasm.entity.ItemEntity;
import dn.jasm.entity.OrderEntity;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.*;

@Data
@ToString
public class ListOrderResponse implements Serializable {
    private List<OrderResponse> orders = new ArrayList<>();

}
