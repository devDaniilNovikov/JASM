package dn.jasm.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dn.jasm.configuration.aop.Loggable;
import dn.jasm.configuration.redis.RedisService;
import dn.jasm.dto.transaction.TransactionDto;
import dn.jasm.entity.OrderEntity;
import dn.jasm.entity.TransactionEntity;
import dn.jasm.entity.UserEntity;
import dn.jasm.entity.enums.OrderStatus;
import dn.jasm.entity.enums.TransactionStatus;
import dn.jasm.event.TransactionEvent;
import dn.jasm.exception.OrderNotFoundException;
import dn.jasm.exception.RedisKeyException;
import dn.jasm.exception.TransactionNotFoundException;
import dn.jasm.exception.UserNotFoundException;
import dn.jasm.mapper.TransactionMapper;
import dn.jasm.repository.OrderRepository;
import dn.jasm.repository.TransactionRepository;
import dn.jasm.repository.UserRepository;
import dn.jasm.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final ApplicationEventPublisher eventPublisher;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RedisService redisService;
    private final TransactionMapper transactionMapper;


    @Override
    public TransactionDto getTransactionById(Long txId) {
        return transactionMapper.mapToDto(
                transactionRepository.findById(txId)
                        .orElseThrow(()->new TransactionNotFoundException(
                                MessageFormat.format("Transaction with id: {0} not found",txId))));
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createTransaction(TransactionDto transactionDto, Long userId) {
        TransactionEntity transactionEntity = new TransactionEntity();
        var user = userRepository.findById(userId)
                .orElseThrow(RuntimeException::new);
        var order = orderRepository.findById(transactionDto.getOrderId())
                        .orElseThrow(RuntimeException::new);
        validateTransactionBalance(user,order);
        transactionEntity.setUserEntity(user);
        transactionEntity.setOrderEntity(order);
        transactionEntity.setCompletedAt(true);
        order.setTransactionEntity(transactionEntity);
        user.setTransactionEntity(transactionEntity);
        transactionEntity.setTransactionStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transactionEntity);
        putToCache(transactionEntity);
        publishTransactionEvent(transactionEntity);
        log.info("Saved transaction is: {}",transactionEntity.getId());


    }
    private void putToCache(TransactionEntity transactionEntity) {
        var txMappingValue = transactionMapper.mapToDto(transactionEntity);
        var txCacheKey = Long.toString(transactionEntity.getId());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try {
            var txCacheValue = objectMapper.writeValueAsString(txMappingValue);
            redisService.writeObjectInRedis(txCacheKey, txCacheValue);
        } catch (JsonProcessingException e) {
            log.error("Can't serialize value: {}", e.getMessage());
        }
    }




    private void validateTransactionBalance(UserEntity user,
                                            OrderEntity order){
        var balance = user.getBalance();
        if (balance.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException(
                    MessageFormat.format("Balance of user: {0} is null",user.getBalance()));
        }
        if (order.getAmount().compareTo(balance)>0){
            throw new IllegalArgumentException(
                    MessageFormat.format(
                            "Insufficient balance {0} for order amount: {1}",
                            balance,order.getAmount()));
        }

    }



    private void publishTransactionEvent(TransactionEntity transactionEntity){
        eventPublisher.publishEvent(
                new TransactionEvent(this,
                        transactionEntity.getId(),
                        transactionEntity.getUserEntity().getId(),
                        true,
                        transactionEntity.getUserEntity().getBalance(),
                        true));
    }



    @Override
    @Loggable
    @Transactional
    public void cancelTransaction(Long txId) {
        TransactionEntity transactionEntity = transactionRepository.findById(txId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        MessageFormat.format("Transaction with id: {0} not found", txId)));
        if (transactionEntity.getUserEntity() != null) {
            UserEntity user = transactionEntity.getUserEntity();
            user.setTransactionEntity(null);
            transactionEntity.setUserEntity(null);
            userRepository.save(user);
        }
        if (transactionEntity.getOrderEntity() != null) {
            OrderEntity order = transactionEntity.getOrderEntity();
            order.setTransactionEntity(null);
            transactionEntity.setOrderEntity(null);
            orderRepository.save(order);
        }
        transactionEntity.setTransactionStatus(TransactionStatus.CANCELLED);
        transactionEntity.setCompletedAt(false);
    }




    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE,
    rollbackFor = {TransactionNotFoundException.class,
    UserNotFoundException.class,OrderNotFoundException.class},
    propagation = Propagation.REQUIRES_NEW)
    public boolean completeTransaction(Long txId, Long userId) {
        return transactionRepository.findById(txId)
                .stream()
                .map(tx->{
                    tx.setCompletedAt(true);
                    tx.setTransactionStatus(TransactionStatus.COMPLETED);
                    var user = userRepository.findById(userId)
                            .orElseThrow(RuntimeException::new);
                    var order = tx.getOrderEntity();
                    tx.setUserEntity(user);
                    tx.setOrderEntity(order);
                    publishTransactionEvent(tx);
                    putToCache(tx);
                    transactionRepository.save(tx);
                    return tx.getCompletedAt();
                }).reduce(false,(t,f)-> true);
    }

    @Override
    @Transactional
    @Loggable
    public void cancelMultipleTransactions(List<Long> txIds) {
        List<TransactionEntity> requireTransactions = transactionRepository
                .findAllById(txIds)
                .stream()
                .map(tx->{
                       tx.setTransactionStatus(TransactionStatus.CANCELLED);
                       tx.setOrderEntity(null);
                       tx.setUserEntity(null);
                       publishTransactionEvent(tx);
                       return transactionRepository.save(tx);
                       })
                .toList();
        transactionRepository.saveAll(requireTransactions);
        log.info("Cancelled transactions is: {}",requireTransactions);

    }
}

















