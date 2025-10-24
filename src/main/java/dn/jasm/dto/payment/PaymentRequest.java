package dn.jasm.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentRequest {

    private String currency;
    private String paymentMethod;
    private Long clientId;
    private String cardNumber;
}
