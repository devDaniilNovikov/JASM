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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.ServletRequestHandledEvent;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private static final String GET_PAYMENT_STATUS = "/api/v1/payment/{id}/status";
    private static final String CREATE_CHARGE = "/api/v1/charge/create";

    private final ApplicationEventPublisher eventPublisher;


    private final PaymentService paymentService;

    @PostMapping(CREATE_CHARGE)
    @ResponseStatus(HttpStatus.CREATED)
    public void createPayment(@RequestBody  UserRequest userRequest,
                              @RequestParam  BigDecimal amount){
        paymentService.createPayment(userRequest, amount);
    }
    
    @GetMapping(GET_PAYMENT_STATUS)
    @ResponseStatus(HttpStatus.OK)
    public String getPaymentStatus(@PathVariable String id){
        return paymentService.getPaymentStatus(id);
                
    }
}