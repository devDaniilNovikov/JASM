package dn.jasm.dto;

import dn.jasm.entity.Summary;
import dn.jasm.entity.enums.OrderStatus;
import dn.jasm.entity.enums.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class SummaryDto {

    private long userId;
    private Map<OrderStatus, List<Summary.PaymentEntry>> values;
}

