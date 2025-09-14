package dn.jasm.service;

import dn.jasm.dto.transaction.TransactionDto;
import dn.jasm.entity.OrderEntity;
import dn.jasm.entity.TransactionEntity;
import dn.jasm.entity.UserEntity;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public interface TransactionService {


    TransactionDto getTransactionById(Long txId);

    void createTransaction(Long userId,Long orderId,Long cardId);

    void cancelTransaction(Long txId);

    void cancelMultipleTransactions(List<Long> txIds);

    LinkedHashSet<TransactionDto> getTransactionSet(int pageNumber, int pageSize);

    void deleteTransaction(Long txId);

    void validateTransactionBalance(UserEntity user, OrderEntity order);

}
