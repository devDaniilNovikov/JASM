package dn.jasm.dto.payment;

import lombok.*;

@Builder
@Data
@ToString(exclude = {"id","status"})
public class PaymentResponse {

    private String id;
    private String status;
}
