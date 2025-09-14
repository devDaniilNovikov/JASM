package dn.jasm.repository;

import dn.jasm.entity.TransactionEntity;
import dn.jasm.entity.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface TransactionRepository extends JpaRepository<TransactionEntity,Long> {

    List<TransactionEntity> findByUserId(Long userId);

    List<TransactionEntity> findByTransactionStatus(TransactionStatus transactionStatus);

    Set<TransactionEntity> findByCardId(Long cardId);
}
