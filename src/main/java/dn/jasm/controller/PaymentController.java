package dn.jasm.controller;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Product;
import com.stripe.model.Subscription;
import dn.jasm.dto.item.ItemRequest;
import dn.jasm.service.PaymentService;
import dn.jasm.dto.user.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.ServletRequestHandledEvent;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private static final String CREATE_PAYMENT = "/api/v1/payment/create-payment";
    private static final String CREATE_CUSTOMER = "/api/v1/payment/create-customer";
    private static final String CREATE_SUBSCRIPTION= "/api/v1/payment/create-subscription";
    private static final String CREATE_PRODUCT= "/api/v1/payment/create-product";
    private static final String CREATE_CHARGE = "/api/v1/charge/create";

    private final ApplicationEventPublisher eventPublisher;


    private final PaymentService paymentService;

    @PostMapping(CREATE_CHARGE)
    public void createPayment(@RequestBody  UserRequest userRequest,
                             @RequestParam  BigDecimal amount,
                             @RequestParam String paymentMethod,
                             @RequestHeader(required = true) Map<String,String> headers){
        paymentService.createPayment(userRequest, amount, headers,paymentMethod);
    }
}