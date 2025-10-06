package dn.jasm.service;

import com.stripe.model.*;
import dn.jasm.dto.item.ItemRequest;
import dn.jasm.dto.user.UserRequest;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentService {

    void createPayment(UserRequest userRequest,
                       BigDecimal amount,
                       Map<String,String> headers,
                       String paymentMethod);
}
