package dn.jasm.service;

import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Product;
import com.stripe.model.Subscription;
import dn.jasm.dto.item.ItemRequest;
import dn.jasm.dto.user.UserRequest;

import java.math.BigDecimal;

public interface PaymentService {

    void createPaymentIntent(UserRequest userRequest,
                             BigDecimal amount,
                             String currency);
}
