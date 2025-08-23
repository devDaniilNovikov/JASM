package dn.jasm.service;

import dn.jasm.dto.transaction.TransactionDto;
import dn.jasm.entity.TransactionEntity;

import java.util.List;

public interface TransactionService {


    TransactionDto getTransactionById(Long txId);

    void createTransaction(TransactionDto transactionDto, Long userId);


    void cancelTransaction(Long txId);

    boolean completeTransaction(Long txId,Long userId);

    void cancelMultipleTransactions(List<Long> txIds);

}
