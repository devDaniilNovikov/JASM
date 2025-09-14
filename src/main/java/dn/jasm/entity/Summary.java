package dn.jasm.entity;

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
public class Summary {

    private long userId;
    private Map<OrderStatus, List<PaymentEntry>> values;

    @NoArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class PaymentEntry{
        private PaymentStatus paymentStatus;
        private double value;
    }

    public Summary(){
        this.values = new HashMap<>();
    }

    public void addValue(OrderStatus orderStatus,PaymentEntry value){
        if (values.containsKey(orderStatus)){
            List<PaymentEntry> paymentEntries = new ArrayList<>(values.get(orderStatus));
            paymentEntries.add(value);
            values.put(orderStatus,paymentEntries);
        }
        values.put(orderStatus,List.of(value));
    }
}
