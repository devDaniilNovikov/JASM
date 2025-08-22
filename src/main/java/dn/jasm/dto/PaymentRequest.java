package dn.jasm.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentRequest {

    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private Long clientId;
}
