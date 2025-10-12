package dn.jasm.service.impl;

import dn.jasm.configuration.redis.RedisSchema;
import dn.jasm.entity.Summary;
import dn.jasm.entity.enums.OrderStatus;
import dn.jasm.entity.enums.PaymentStatus;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {

    private final JedisPool jedisPool;


    @Override
    public Summary get(long userId,
                       Set<OrderStatus> orderStatuses,
                       Set<PaymentStatus> paymentStatuses) {
        return findByUserId(userId,
                orderStatuses == null ? Set.of(OrderStatus.values()) : orderStatuses,
                paymentStatuses == null ? Set.of(PaymentStatus.values()) : paymentStatuses)
                .orElseThrow(()->new UserNotFoundException(
                        MessageFormat.format("[User with id: {0} not found]",userId)
                ));
    }

    public Optional<Summary> findByUserId(long userId,
                                          Set<OrderStatus> orderStatuses,
                                          Set<PaymentStatus> paymentStatuses) {
        try (Jedis jedis = jedisPool.getResource()){
            if (jedis.sismember(RedisSchema.userKeys(), String.valueOf(userId))){
                return Optional.empty();
            }
            if (orderStatuses.isEmpty() && !paymentStatuses.isEmpty()){
                return getSummary(userId,Set.of(OrderStatus.values()),paymentStatuses,jedis);
            }
            else if (!orderStatuses.isEmpty() && paymentStatuses.isEmpty()){
                return getSummary(userId,orderStatuses,Set.of(PaymentStatus.values()),jedis);
            }
            else {
                return getSummary(userId,orderStatuses,paymentStatuses,jedis);
            }
        }
    }

    private Optional<Summary> getSummary(
            long userId,
            Set<OrderStatus> orderStatuses,
            Set<PaymentStatus> paymentStatuses,
            Jedis jedis
    ){
        Summary summary = new Summary();
        summary.setUserId(userId);
        for (OrderStatus orderStatus:orderStatuses){
            for (PaymentStatus paymentStatus:paymentStatuses){
                Summary.PaymentEntry summaryEntry = new Summary.PaymentEntry();
                summaryEntry.setPaymentStatus(paymentStatus);
                String value = jedis.hget(
                        RedisSchema.paymentKey(userId,orderStatus),
                        paymentStatus.name().toLowerCase());
                if (value!=null){
                    summaryEntry.setValue(Double.parseDouble(value));
                }
                summary.addValue(orderStatus,summaryEntry);
            }
        }
        return Optional.of(summary);
    }
}
