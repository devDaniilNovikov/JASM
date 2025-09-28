package dn.jasm.dto.ticket;


import dn.jasm.dto.order.OrderResponse;
import dn.jasm.entity.TicketEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
public class MapTicketDto {

    private Map<String, OrderResponse> ticketMap = new TreeMap<>();

}
