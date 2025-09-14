package dn.jasm.service.scheduling.impl;

import dn.jasm.repository.CardRepository;
import dn.jasm.service.RedisService;
import dn.jasm.entity.TransactionEntity;
import dn.jasm.entity.enums.TransactionStatus;
import dn.jasm.repository.TransactionRepository;
import dn.jasm.service.scheduling.TransactionScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionSchedulerImpl implements TransactionScheduler {

    private final RedisService redisService;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;


    @Override
//    @Scheduled(fixedDelay = 10000L)
    @Transactional
    public void cleanCancelledTransactions() {
        Set<String> transactionsForDelete = transactionRepository.findByTransactionStatus(TransactionStatus.CANCELLED)
                .stream()
                .map(tx-> {
                    tx.getUser().setTransactionEntity(null);
                    tx.getOrderEntity().setTransactionEntity(null);
                    long txCount = tx.getCard().getTransactions().size();
                    var txCountByCard = tx.getCard();
                    txCountByCard.setTransactions(new HashSet<>((int) txCount));
                    cardRepository.save(txCountByCard);
                    log.info("Count after update of transactions on card: {}",txCount);

                return transactionRepository.save(tx);})
                .map(TransactionEntity::getId)
                .map(String::valueOf)
                .collect(Collectors.toSet());
        redisService.deleteCachesByKeys(transactionsForDelete);
        var keys = transactionsForDelete.stream().map(Long::valueOf).toList();
        log.info("Ids of deleted transactions: {}",keys);
        transactionRepository.deleteAllByIdInBatch(keys);
    }
}
