package dn.jasm.mapper;

import com.stripe.model.PaymentIntent;
import dn.jasm.entity.CardEntity;
import dn.jasm.entity.PaymentEntity;
import dn.jasm.entity.enums.PaymentStatus;
import dn.jasm.exception.CardNotFoundException;
import dn.jasm.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Component
public class PaymentMapper {

    private final CardRepository cardRepository;



    public PaymentIntent mapToPaymentDto(PaymentEntity paymentEntity){
        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setAmount(paymentEntity.getAmount().longValue());
        paymentIntent.setCurrency(paymentEntity.getCurrency());
        paymentIntent.setId(String.valueOf(paymentEntity.getId()));
        paymentIntent.setStatus(PaymentStatus.PROCESSING.name());
        return paymentIntent;
    }

    public PaymentEntity mapToPaymentEntity(PaymentIntent paymentIntent,
                                            CardEntity card){
        PaymentEntity payment = new PaymentEntity();
        payment.setAmount(BigDecimal.valueOf(paymentIntent.getAmount()));
        payment.setCurrency(paymentIntent.getCurrency());
        payment.setId(payment.getId());
        var cardForPayment = cardRepository.findById(card.getId())
                .orElseThrow(CardNotFoundException::new);
        payment.setCard(cardForPayment);
        return payment;
    }


}
