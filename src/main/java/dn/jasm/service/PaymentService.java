package dn.jasm.service;

import com.stripe.model.*;
import dn.jasm.dto.item.ItemRequest;
import dn.jasm.dto.user.UserRequest;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public interface PaymentService {

    void createPayment(UserRequest userRequest,
                       BigDecimal amount,
                       Map<String,String> headers);

    PaymentIntent cancelPayment(String paymentId);

    PaymentIntent confirmPayment(String paymentId,
                                 String paymentMethod);

    String getPaymentStatus(String paymentId);

}
