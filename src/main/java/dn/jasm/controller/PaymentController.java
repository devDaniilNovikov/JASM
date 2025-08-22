package dn.jasm.controller;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Product;
import com.stripe.model.Subscription;
import dn.jasm.dto.item.ItemRequest;
import dn.jasm.service.PaymentService;
import dn.jasm.dto.user.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private static final String CREATE_PAYMENT = "/api/v1/payment/create-payment";
    private static final String CREATE_CUSTOMER = "/api/v1/payment/create-customer";
    private static final String CREATE_SUBSCRIPTION= "/api/v1/payment/create-subscription";
    private static final String CREATE_PRODUCT= "/api/v1/payment/create-product";


    private final PaymentService paymentService;


    @PostMapping(CREATE_PAYMENT)
    public ResponseEntity<PaymentIntent> createPayment(@RequestParam BigDecimal amount,
                                                       @RequestParam String currency,
                                                       @RequestParam(value = "payment_method") String paymentMethod) {
        return ResponseEntity.ok(paymentService.createPayment(amount,currency,paymentMethod));
    }
    @PostMapping(CREATE_CUSTOMER)
    public ResponseEntity<Customer> createCustomer(@RequestBody UserRequest request) {
        return ResponseEntity.ok(paymentService.createCustomer(request));
    }
    @PostMapping(CREATE_SUBSCRIPTION)
    public ResponseEntity<Subscription> createSubscription(@RequestParam Long clientID,
                                                           @RequestParam BigDecimal amount,
                                                           @RequestParam Long quantity) {
        return ResponseEntity.ok(paymentService.createSubscription(clientID,amount,quantity));
    }
    @PostMapping(CREATE_PRODUCT)
    public ResponseEntity<Product> createProduct(@RequestBody ItemRequest request,@RequestParam Long price){
        return ResponseEntity.ok(paymentService.createProduct(request,price));
    }
}