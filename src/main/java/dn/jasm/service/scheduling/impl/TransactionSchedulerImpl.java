package dn.jasm.service.scheduling.impl;

import dn.jasm.configuration.redis.RedisService;
import dn.jasm.entity.TransactionEntity;
import dn.jasm.entity.enums.TransactionStatus;
import dn.jasm.repository.TransactionRepository;
import dn.jasm.service.scheduling.TransactionScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionSchedulerImpl implements TransactionScheduler {

    private final RedisService redisService;
    private final TransactionRepository transactionRepository;

    @Override
    @Scheduled(fixedDelay = 10000L)
    @Transactional
    public void cleanCancelledTransactions() {
        Set<String> transactionsForDelete = transactionRepository.findAll()
                .stream()
                    .filter(t->t.getTransactionStatus().equals(TransactionStatus.COMPLETED))
                .map(tx-> {
                    tx.getUserEntity().setTransactionEntity(null);
                    tx.getOrderEntity().setTransactionEntity(null);
                return transactionRepository.save(tx);})
                .map(TransactionEntity::getId)
                .map(String::valueOf)
                .collect(Collectors.toSet());
        redisService.deleteCachesByKeys(transactionsForDelete.stream().toList());
        var keys = transactionsForDelete.stream().map(Long::valueOf).toList();
        log.info("Ids of deleted transactions: {}",keys);
        transactionRepository.deleteAllByIdInBatch(keys);
    }
}
