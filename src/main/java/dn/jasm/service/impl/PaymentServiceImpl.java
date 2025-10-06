package dn.jasm.service.impl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.JsonObject;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.net.ApiRequestParams.EnumParam.*;
import com.stripe.model.*;
import com.stripe.param.ChargeCreateParams;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.SetupIntentCreateParams.PaymentMethodOptions.AcssDebit.Currency;
import com.stripe.param.SetupIntentUpdateParams;
import dn.jasm.event.PaymentEvent;
import dn.jasm.repository.*;
import dn.jasm.dto.user.UserRequest;
import dn.jasm.service.ItemService;
import dn.jasm.service.OrderService;
import dn.jasm.service.PaymentService;
import dn.jasm.service.RedisService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import com.google.gson.*;
import org.springframework.web.context.support.ServletRequestHandledEvent;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Value("${stripe.secret.api-key}")
    private String apiKey;

    @Value("${account.secret.id}")
    private String accountId;

    private static final String CURRENCY = Currency.USD.getValue().toLowerCase();
    private static final String RU_LOCALE = "ru";

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
    private final RestClient restClient;
    private final ApplicationEventPublisher eventPublisher;



    @PostConstruct
    public void init() {
        Stripe.apiKey = this.apiKey;
    }


    @Override
    @Transactional
    public void createPayment(UserRequest userRequest,
                               BigDecimal amount,
                              Map<String,String> headers,
                              String paymentMethod) {
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(userRequest.getEmail())
                    .setPhone(userRequest.getPhoneNumber())
                    .setName(userRequest.getUsername())
                    .setPaymentMethod(paymentMethod)
                    .build();
            PaymentIntentCreateParams createParams = PaymentIntentCreateParams
                    .builder()
                    .setAmount(Long.valueOf(String.valueOf(amount)))
                    .setCurrency(CURRENCY)
                    .setCustomer(params.getName())
                    .build();
            Map<String,String> metadata = new HashMap<>();
            metadata.put("amount",String.valueOf(amount));
            metadata.put("currency",CURRENCY);
            metadata.put("confirmed_at", String.valueOf(createParams.getConfirm()));
            metadata.put("client_name",params.getName());
            addHeaders(headers);
            log.info("Added headers is: {}",metadata);
            var pay = PaymentIntent.create(createParams);
            pay.setMetadata(metadata);
            publishEvent(amount,params.getPaymentMethod()
                    ,userRequest.getEmail(),
                    userRequest.getCardNumber());
            var paymentJsonString = mapFromGsonToJackson(pay);
            objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            redisService.writeObjectInRedis(pay.getId(), paymentJsonString);
            log.info("Created payment: {}", pay.getAmount());
        } catch (StripeException e) {
            log.error("Exception is: {}", e.getMessage());
        }
    }



    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addHeaders(Map<String,String> headers){
        if (!headers.containsValue(apiKey)){
            throw new IllegalArgumentException("Headers can't be null!");
        }
        MultiValueMap<String,String> map = new LinkedMultiValueMap<>();
        for(Map.Entry<String,String> headerMap:headers.entrySet()){
            String key = headerMap.getKey();
            String value = headerMap.getValue();
            map.put(key,List.of(value));
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(map);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setCacheControl(CacheControl.maxAge(Duration.ofMinutes(5)));
        httpHeaders.setContentLanguage(Locale.of(RU_LOCALE));
        httpHeaders.set("X-Request-ID",apiKey);
        log.info("Http Headers is: {}, {}, {}",headers.keySet(),headers.entrySet(),httpHeaders.asSingleValueMap());
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

    private void publishEvent(BigDecimal amount,
                              String paymentMethod,
                              String email,
                              String cardNumber){
        eventPublisher.publishEvent(new PaymentEvent(
                this,
                amount,
                paymentMethod,
                email,
                cardNumber
        ));
    }




}








