package dn.jasm.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.stripe.Stripe;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import com.stripe.net.StripeResponse;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import dn.jasm.exception.PaymentMethodException;
import dn.jasm.configuration.StripeResponseSerializer;
import dn.jasm.repository.CardRepository;
import dn.jasm.entity.enums.CardType;
import dn.jasm.repository.ItemRepository;
import dn.jasm.dto.item.ItemRequest;
import dn.jasm.repository.PaymentRepository;
import dn.jasm.entity.OrderEntity;
import dn.jasm.entity.UserEntity;
import dn.jasm.repository.UserRepository;
import dn.jasm.dto.user.UserRequest;
import dn.jasm.service.ItemService;
import dn.jasm.service.OrderService;
import dn.jasm.service.PaymentService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    @Value("${stripe.secret.api-key}")
    private String apiKey;

    @Value("${account.secret.id}")
    private String accountId;

    private static final String ACTUAL_CURRENCY = "rub";

    @PostConstruct
    public void init(){
        Stripe.apiKey = apiKey;
    }


    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ItemRepository itemRepository;
    private final CardRepository cardRepository;
    private final ItemService itemService;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;


    @Override
    public PaymentIntent createPayment(BigDecimal amount,String currency,String paymentMethod) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("amount", amount);
            params.put("currency", currency);
            params.put("payment_method", paymentMethod);
            log.info("Created params: {}", params);
            RequestOptions requestOptions = RequestOptions.builder()
                    .setStripeAccount(accountId)
                    .setApiKey(apiKey)
                    .build();
            return PaymentIntent.create(params,requestOptions);
        } catch (StripeException e) {
            if (e.getCode().equals("resource_missing") && "card".equals(paymentMethod)) {
                log.error("PaymentMethod 'card' is missing: {}", e.getMessage());
                throw new PaymentMethodException("PaymentMethod 'card' is not available");
            }
            log.error("Can't make payment because: {}", e.getMessage());
            throw new RuntimeException();
        } catch (Exception e) {
            log.error("Error during make payment creation: {}", e.getMessage());
            throw new RuntimeException();
        }
    }



    @Override
    public Customer createCustomer(UserRequest userRequest) {
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setName(userRequest.getUsername())
                    .setEmail(userRequest.getEmail())
                    .setPaymentMethod(CardType.DEBIT.name())
                    .setPhone(userRequest.getPhoneNumber())
                    .build();
            return Customer.create(params);
        } catch (Exception e) {
            log.error("Cant create customer cause: {}", e.getMessage());
            throw new RuntimeException();
        }
    }

    @Override
    public Subscription createSubscription(Long clientId,BigDecimal amount,Long quantity) {
        try {
            final BigDecimal totalAmount = userRepository.findById(clientId)
                    .stream()
                    .map(UserEntity::getOrders)
                    .flatMap(Collection::stream)
                    .map(OrderEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                    .addItem(SubscriptionCreateParams.Item
                            .builder()
                            .setPrice(String.valueOf(totalAmount))
                            .setQuantity(quantity)
                            .build())
                    .build();
            return Subscription.create(params);
        } catch (Exception e) {
            log.error("Can't create subscription: {}", e.getMessage());
            throw new RuntimeException();
        }
    }


    @Override
    public Product createProduct(ItemRequest itemRequest, Long amount) {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(StripeResponse.class,new StripeResponseSerializer());
        objectMapper.registerModule(simpleModule);
        try {
            ProductCreateParams params = ProductCreateParams.builder()
                    .setName(itemRequest.getName())
                    .setShippable(itemRequest.getIsShippable())
                    .setDescription(itemRequest.getDescription())
                    .build();
            Product product = Product.create(params);
            PriceCreateParams priceCreateParams = PriceCreateParams.builder()
                    .setNickname(itemRequest.getName())
                    .setProduct(product.getId())
                    .setUnitAmount(amount)
                    .setCurrency(ACTUAL_CURRENCY)
                    .setRecurring(PriceCreateParams
                            .Recurring.builder()
                            .setInterval(PriceCreateParams.Recurring.Interval.DAY)
                            .build())
                    .build();
            Price price = Price.create(priceCreateParams);
            price.setLivemode(true);
            var item = itemService.createItem(price, product);
            var order = orderService.createOrder(price,List.of(item));
            product.setActive(true);
            if (product.getActive()){
                order.setIsPayed(true);
            }
            return product;
        } catch (CardException e) {
            String message = e.getMessage();
            String code = e.getCode();
            log.error("Ошибка карты: {}, код ошибки: {}", message, code);
            throw new RuntimeException(e);
        } catch (StripeException e) {
            log.error("Can't create product: {}, error: {}", e.getMessage(), e.getStatusCode());
            throw new RuntimeException(e);
        }
    }

}
