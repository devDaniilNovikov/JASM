package dn.jasm.service;

import dn.jasm.dto.transaction.SetTransactionDto;
import dn.jasm.dto.transaction.TransactionDto;
import dn.jasm.entity.OrderEntity;
import dn.jasm.entity.UserEntity;
import dn.jasm.event.TransactionEvent;

import java.util.List;

public interface TransactionService {


    TransactionDto getTransactionById(Long txId);

    void createTransaction(Long userId,
                           Long orderId,
                           Long cardId);

    void cancelTransaction(Long txId);

    void cancelMultipleTransactions(List<Long> txIds);

    SetTransactionDto getTransactionSet(int pageNumber,
                                        int pageSize);

    void deleteTransaction(Long txId);

    void validateTransactionBalance(UserEntity user,
                                    OrderEntity order);

    void handleTransactionEvent(TransactionEvent transactionEvent);

}
