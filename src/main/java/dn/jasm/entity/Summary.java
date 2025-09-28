package dn.jasm.entity;

import dn.jasm.entity.enums.OrderStatus;
import dn.jasm.entity.enums.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

@Getter
@Setter
public class Summary {

    private long userId;
    private Map<OrderStatus, List<PaymentEntry>> values;

    @Override
    public String toString() {
        return "Summary{" +
                "userId=" + userId +
                ", values=" + values +
                '}';
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class PaymentEntry {
        private PaymentStatus paymentStatus;
        private double value;

        @Override
        public String toString() {
            return "PaymentEntry{" +
                    "paymentStatus=" + paymentStatus.name().toLowerCase() +
                    ", value=" + value +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PaymentEntry that = (PaymentEntry) o;
            return getPaymentStatus() == that.getPaymentStatus();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getPaymentStatus());
        }
    }

    public Summary(){
        this.values = new HashMap<>();
    }

    public void addValue(OrderStatus orderStatus,PaymentEntry value){
        if (orderStatus == null){
            throw new IllegalArgumentException("Status can't be null");
        }
        if (values.containsKey(orderStatus)){
            List<PaymentEntry> paymentEntries = new ArrayList<>(values.get(orderStatus));
            paymentEntries.add(value);
            values.put(orderStatus,paymentEntries);
        }
        values.put(orderStatus,List.of(value));
    }



}
