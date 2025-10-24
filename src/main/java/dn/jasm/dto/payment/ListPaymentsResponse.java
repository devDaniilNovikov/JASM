package dn.jasm.dto.payment;

import lombok.Data;
import java.util.*;

@Data
public class ListPaymentsResponse {

    private List<PaymentResponse> payments;
}
