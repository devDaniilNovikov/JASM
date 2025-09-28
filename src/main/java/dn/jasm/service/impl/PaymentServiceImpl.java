package dn.jasm.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.JsonObject;
import com.stripe.Stripe;
import com.stripe.StripeClient;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.HttpHeaders;
import com.stripe.net.RequestOptions;
import com.stripe.net.StripeResponse;
import com.stripe.param.*;
import dn.jasm.exception.OrderNotFoundException;
import dn.jasm.exception.PaymentMethodException;
import dn.jasm.configuration.StripeResponseSerializer;
import dn.jasm.repository.*;
import dn.jasm.entity.enums.CardType;
import dn.jasm.dto.item.ItemRequest;
import dn.jasm.entity.OrderEntity;
import dn.jasm.entity.UserEntity;
import dn.jasm.dto.user.UserRequest;
import dn.jasm.service.ItemService;
import dn.jasm.service.OrderService;
import dn.jasm.service.PaymentService;
import dn.jasm.service.RedisService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.net.Proxy;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;

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

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final CardRepository cardRepository;
    private final ItemService itemService;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    private final RedisService redisService;
    private final SecureRandom secureRandom = new SecureRandom();


    @PostConstruct
    public void init() {
        Stripe.apiKey = this.apiKey;
    }


    @Override
    public void createPaymentIntent(UserRequest userRequest,
                                    BigDecimal amount,
                                    String currency) {
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(userRequest.getEmail())
                    .setPhone(userRequest.getPhoneNumber())
                    .setName(userRequest.getUsername())
                    .build();
            PaymentIntentCreateParams createParams = PaymentIntentCreateParams
                    .builder()
                    .setAmount(Long.valueOf(String.valueOf(amount)))
                    .setCurrency(currency)
                    .setCustomer(params.getName())
                    .build();
            var pay = PaymentIntent.create(createParams);
            Map<String,String> metadata = new HashMap<>();
            metadata.put("amount",String.valueOf(amount));
            metadata.put("currency",currency);
            metadata.put("confirmed_at", String.valueOf(createParams.getConfirm()));
            metadata.put("client_name",params.getName());
            pay.setMetadata(metadata);
            var paymentJsonString = mapFromGsonToJackson(pay);
            log.info("Payment id: {}", pay.getId());
            if (pay.getId() == null) {
                var payId = String.valueOf(secureRandom.nextLong(1000000));
                log.info("Payment id: {}", payId);
                pay.setId(payId);
                redisService.writeObjectInRedis(pay.getId(), paymentJsonString);
            }
            objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            redisService.writeObjectInRedis(pay.getId(), paymentJsonString);
            log.info("Created payment: {}", pay.getAmount());
        } catch (StripeException e) {
            log.error("Exception is: {}", e.getMessage());
        }
    }


    private String mapFromGsonToJackson(PaymentIntent paymentIntent) {
        try {
            JsonObject jsonObject = paymentIntent.getRawJsonObject();
            String gsonString = jsonObject.toString();
            JsonNode jsonNode = objectMapper.readTree(gsonString);
            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            log.error("Json exception is: {}", e.getMessage());
            return null;
        }

    }
}








