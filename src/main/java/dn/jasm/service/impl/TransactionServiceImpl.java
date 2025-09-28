package dn.jasm.service.impl;

import dn.jasm.configuration.aop.Loggable;
import dn.jasm.dto.transaction.SetTransactionDto;
import dn.jasm.entity.CardEntity;
import dn.jasm.exception.CardNotFoundException;
import dn.jasm.repository.CardRepository;
import dn.jasm.service.RedisService;
import dn.jasm.dto.transaction.TransactionDto;
import dn.jasm.entity.OrderEntity;
import dn.jasm.entity.TransactionEntity;
import dn.jasm.entity.UserEntity;
import dn.jasm.entity.enums.TransactionStatus;
import dn.jasm.event.TransactionEvent;
import dn.jasm.exception.OrderNotFoundException;
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
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CardRepository cardRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisService redisService;
    private final TransactionMapper transactionMapper;



    @Override
    public TransactionDto getTransactionById(Long txId) {
        return transactionMapper.mapToDto(transactionRepository.findById(txId)
                        .orElseThrow(()->new TransactionNotFoundException(
                                MessageFormat.format("Transaction with id: {0} not found",txId))));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createTransaction(Long userId,
                                  Long orderId,
                                  Long cardId) {
        var user = userRepository.findById(userId)
                        .orElseThrow(()->new UserNotFoundException(
                         MessageFormat.format(
                                 "User with id: {0} not found",userId)));
        var order = orderRepository.findById(orderId)
                        .orElseThrow(()->new OrderNotFoundException(
                         MessageFormat.format(
                                 "Order with id: {0} not found",orderId)));
        var card = cardRepository.findById(cardId).orElseThrow(CardNotFoundException::new);
        validateTransactionBalance(user,order);
        var txDto = createTx(order,user,card);
        publishEvent(txDto,order,user);
        redisService.writeObjectInRedis(String.valueOf(txDto.getTxId()),txDto.toString());
        log.info("Created tx: {} for order: {} and user: {} with card: {}",
                txDto.getTxId(),
                order.getId(),
                user.getId(),
                card.getId());
    }


    @Async
    public void publishEvent(TransactionDto transactionDto,
                              OrderEntity order,
                              UserEntity user){
        eventPublisher.publishEvent(new TransactionEvent(
                this,
                transactionDto.getTxId(),
                user.getId(),
                true,
                order.getAmount(),
                true,
                order.getId(),
                transactionDto.getCardId()
        ));
    }


    public TransactionDto createTx(OrderEntity order, UserEntity user, CardEntity cardEntity){
        TransactionEntity tx = new TransactionEntity();
        tx.setTotalAmount(order.getAmount());
        tx.setTransactionStatus(TransactionStatus.PROCESSING);
        tx.setCompletedAt(true);
        var card = cardRepository.findById(cardEntity.getId()).orElseThrow(CardNotFoundException::new);
        tx.setOrderEntity(order);
        order.setTransactionEntity(tx);
        List<TransactionEntity> transactions = user.getTransactionEntity();
        Set<TransactionEntity> transactionsSet = card.getTransactions();
        transactions.add(tx);
        transactionsSet.add(tx);
        transactionRepository.save(tx);
        tx.setUser(user);
        tx.setCard(card);
        userRepository.save(user);
        orderRepository.save(order);
        cardRepository.save(card);
        return transactionMapper.mapToDto(tx);

    }


//    private void putToCache(TransactionDto transactionDto) {
//        EventServiceImpl.putInRedis(transactionDto, redisService);
//    }

    public void validateTransactionBalance(UserEntity user,
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

    @EventListener
    @Override
    public void handleTransactionEvent(TransactionEvent transactionEvent){
        BigDecimal userTotalBalance = userRepository.findById(transactionEvent.getUserId())
                .stream()
                .map(user->{
                    BigDecimal totalBalance = user.getBalance().subtract(transactionEvent.getTotalAmount());
                    user.setBalance(totalBalance);
                    return userRepository.save(user);
                })
                .map(UserEntity::getBalance)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        if (transactionEvent.getCompletedAt()){
            try {
                var txId = transactionEvent.getTxId();
                var transaction = transactionRepository.findById(txId)
                        .orElseThrow(() -> new TransactionNotFoundException(
                                MessageFormat.format("Transaction with id: {0} not found", transactionEvent.getTxId())
                        ));
                var cardForTx = cardRepository.findById(transactionEvent.getCardId())
                        .orElseThrow(RuntimeException::new);
                transaction.setTransactionStatus(TransactionStatus.COMPLETED);
                transaction.setCard(cardForTx);
                transactionRepository.save(transaction);
                var txStatus = transaction.getTransactionStatus();
                var cacheTx = transactionMapper.mapToDto(transaction);
                redisService.writeObjectInRedis(String.valueOf(txId),cacheTx);
                log.info("Successfully processing of transaction with id: {}, status: {} ", txId, txStatus);
            } catch (Exception e) {
                log.error("Exception in processing of transaction: {}",e.getMessage());
                throw new RuntimeException(e);
            }
        }
        log.info("Total balance of user is: {}",userTotalBalance);
    }


    @Override
    @Loggable
    @Transactional
    public void cancelTransaction(Long txId) {
        TransactionEntity transactionEntity = transactionRepository.findById(txId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        MessageFormat.format("Transaction with id: {0} not found", txId)));
        if (transactionEntity.getUser() != null) {
            UserEntity user = transactionEntity.getUser();
            user.setTransactionEntity(null);
            transactionEntity.setUser(null);
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
    @Transactional
    @Loggable
    public void cancelMultipleTransactions(List<Long> txIds) {
        List<TransactionEntity> requireTransactions = transactionRepository.findAllById(txIds)
                .stream()
                .map(tx->{
                       tx.setTransactionStatus(TransactionStatus.CANCELLED);
                       tx.setOrderEntity(null);
                       tx.setUser(null);
//                       publishTransactionEvent(tx);
                       return transactionRepository.save(tx);
                       })
                .toList();
        transactionRepository.saveAll(requireTransactions);
        log.info("Cancelled transactions is: {}",requireTransactions);

    }

    @Override
    public SetTransactionDto getTransactionSet(int pageNumber, int pageSize) {
        if (pageSize==0){
            throw new IllegalArgumentException("PageSize can't be null");
        }
        PageRequest pageRequest = PageRequest.ofSize(pageSize).withPage(pageNumber);
        Set<TransactionEntity> txSet = transactionRepository.findAll(pageRequest)
                .stream()
                .sorted(Comparator.comparing(tx->tx.getUser().getId()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return transactionMapper.mapToDtoSet(txSet);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long txId) {
        var tx = transactionRepository.findById(txId)
                .orElseThrow(RuntimeException::new);

        if (tx.getUser() != null) {
            tx.getUser().setTransactionEntity(null);
            tx.setUser(null);
        }

        if (tx.getOrderEntity() != null) {
            tx.getOrderEntity().setTransactionEntity(null);
            tx.setOrderEntity(null);
        }
        transactionRepository.save(tx);
        transactionRepository.delete(tx);
        log.info("Deleted tx is: {}", tx.getId());
    }
}

















