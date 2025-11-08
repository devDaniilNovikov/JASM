package dn.jasm.service.impl;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.JsonObject;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.net.ApiRequestParams.EnumParam.*;
import com.stripe.model.*;
import com.stripe.net.HttpHeaders;
import com.stripe.net.RequestOptions;
import com.stripe.param.*;
import com.stripe.param.SetupIntentCreateParams.PaymentMethodOptions.AcssDebit.Currency;
import dn.jasm.dto.payment.PaymentResponse;
import dn.jasm.dto.user.UserResponse;
import dn.jasm.entity.CardEntity;
import dn.jasm.entity.PaymentEntity;
import dn.jasm.entity.enums.PaymentStatus;
import dn.jasm.event.PaymentEvent;
import dn.jasm.exception.CardNotFoundException;
import dn.jasm.mapper.PaymentMapper;
import dn.jasm.mapper.UserMapper;
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
import org.springframework.retry.annotation.Retryable;
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
import java.text.MessageFormat;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Value("${stripe.secret.api-key}")
    private String apiKey;

    @Value("${stripe.account.key}")
    private String accountId;

    private static final String RETURN_URL = "http://localhost:3000/api/v1/charge/create";
    private static final String PAYMENT_METHOD = "pm_card_visa";
    private static final String PAYMENT_METHOD_TYPE = "card";


    private static final String USD_CURRENCY = Currency
            .USD
            .getValue()
            .trim()
            .toLowerCase();

    private final ObjectMapper objectMapper;
    private final RedisService redisService;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final CardRepository cardRepository;



    @PostConstruct
    public void init() {
        Stripe.apiKey = this.apiKey;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createPayment(UserRequest userRequest,
                              BigDecimal amount) {
            CardEntity card = cardRepository.findByCardNumber(userRequest.getCardNumber())
                .orElseThrow(CardNotFoundException::new);
            PaymentIntent payment = buildPayment(userRequest, amount, accountId);
            PaymentEntity paymentEntity = paymentMapper.mapToPaymentEntity(payment, card);
            paymentRepository.save(paymentEntity);
            log.info("[Saved payment: {}]", paymentEntity.getId());
            publishEvent(amount, userRequest.getEmail(), userRequest.getCardNumber());
            String paymentJsonString = mapFromGsonToJackson(payment);
            objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            redisService.writeObjectInRedis(payment.getId(), paymentJsonString);
            log.info("[Created payment: {}]", payment.getAmount());
    }


    @Override
    @Transactional
    @JsonIgnoreProperties(value = "lastResponse")
    public PaymentIntent cancelPayment(String paymentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentId);
            PaymentIntentCancelParams params = PaymentIntentCancelParams.builder()
                    .setCancellationReason(PaymentIntentCancelParams
                    .CancellationReason.REQUESTED_BY_CUSTOMER)
                    .build();

            return paymentIntent.cancel(params);
        }catch (StripeException e){
            log.error("[Can't make payment: {}, error: {}]",paymentId,
                    e.getStripeError().getMessage());
            return null;
        }
    }





    @Override
    @Transactional(readOnly = true,rollbackFor = StripeException.class)
    public PaymentResponse getPaymentStatus(String paymentId) {
        try {
            var paymentIntent = PaymentIntent.retrieve(paymentId);
            var payment = paymentRepository.findById(paymentId)
                            .orElseThrow();
            var response = PaymentResponse.builder()
                            .id(payment.getId())
                            .status(paymentIntent.getStatus()
                            .replace("_"," ")
                            .toUpperCase())
                            .build();
            log.info("[Payment status: {}]",paymentIntent.getStatus());
            return response;
        }catch (StripeException e){
            log.error("[Can't get status of payment: {}, error: {}]",paymentId,e.getMessage());
            return null;
        }
    }

    public Customer buildCustomer(UserRequest userRequest){
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(userRequest.getEmail())
                    .setPhone(userRequest.getPhoneNumber())
                    .setName(userRequest.getUsername())
                    .build();
            return Customer.create(params);
        } catch (StripeException e) {
            log.error("[Can't create customer: {}, error: {}]",e.getMessage(),e.getStripeError());
            throw new RuntimeException();
        }
    }

    public PaymentIntent buildPayment(UserRequest userRequest,
                                      BigDecimal amount,
                                      String apiId){
            try {
                RequestOptions requestOptions = null;
                if (accountId != null) {
                    requestOptions = RequestOptions.builder()
                            .setApiKey(apiKey)
                            .setStripeAccount(accountId)
                            .build();
                }
                PaymentIntentCreateParams createParams = PaymentIntentCreateParams
                        .builder()
                        .setAmount(Long.valueOf(
                                String.valueOf(
                                        amount)
                        ))
                        .setCurrency(USD_CURRENCY)
                        .setCustomer(userRequest.getUsername())
                        .setReceiptEmail(userRequest.getEmail())
                        .addPaymentMethodType(PAYMENT_METHOD_TYPE)
                        .setPaymentMethod(PAYMENT_METHOD)
                        .build();
                var payment = PaymentIntent.create(createParams, requestOptions);
                var client = buildCustomer(userRequest);
                payment.setCustomer(client.getId());
                Map<String, String> metadata = addMetadata(amount, USD_CURRENCY, client.getId());
                payment.setMetadata(metadata);
                log.error("[Metadata keys: {}, value: {}]", metadata.keySet(), metadata.values());
                return payment;
            }catch(StripeException e){
                log.error("[Can't build payment, error: {}".toUpperCase(), e.getMessage());
                throw new RuntimeException();
            }

    }

    private String mapFromGsonToJackson(PaymentIntent paymentIntent) {
        try {
            JsonObject jsonObject = paymentIntent.getRawJsonObject();
            String gsonString = jsonObject.toString();
            JsonNode jsonNode = objectMapper.readTree(gsonString);
            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            log.error("[Json exception is: {}]", e.getMessage());
            return null;
        }

    }

    private void publishEvent(BigDecimal amount,
                              String email,
                              String cardNumber){
        eventPublisher.publishEvent(new PaymentEvent(
                this,
                amount,
                email,
                cardNumber
        ));
    }

    public Map<String,String> addMetadata(BigDecimal amount,
                            String currency,
                            String customerId){

        Map<String,String> headers = new HashMap<>();
        headers.put("amount",String.valueOf(amount));
        headers.put("currency",currency);
        headers.put("customerId",customerId);
        return headers;
    }








}








