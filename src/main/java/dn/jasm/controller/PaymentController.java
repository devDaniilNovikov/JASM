package dn.jasm.controller;
import com.stripe.model.PaymentIntent;
import dn.jasm.dto.payment.PaymentResponse;
import dn.jasm.service.PaymentService;
import dn.jasm.dto.user.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private static final String GET_PAYMENT_STATUS = "/api/v1/payments/{id}/status";
    private static final String CREATE_CHARGE = "/api/v1/payments/charge/create";
    private static final String CANCEL_PAYMENT = "/api/v1/payments/{paymentId}/cancel";


    private final PaymentService paymentService;

    @PostMapping(CREATE_CHARGE)
    @ResponseStatus(HttpStatus.CREATED)
    public void createPayment(@RequestBody  UserRequest userRequest,
                              @RequestParam BigDecimal amount){
        paymentService.createPayment(userRequest, amount);
    }
    
    @GetMapping(GET_PAYMENT_STATUS)
    @ResponseStatus(HttpStatus.OK)
    public PaymentResponse getPaymentStatus(@PathVariable String id){
        return paymentService.getPaymentStatus(id);
    }

    @PostMapping(CANCEL_PAYMENT)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public PaymentIntent cancelPayment(@PathVariable String paymentId){
        return paymentService.cancelPayment(paymentId);
    }

}