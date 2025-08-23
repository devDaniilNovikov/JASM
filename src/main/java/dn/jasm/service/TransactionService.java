package dn.jasm.service;

import dn.jasm.dto.transaction.TransactionDto;
import dn.jasm.entity.TransactionEntity;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public interface TransactionService {


    TransactionDto getTransactionById(Long txId);

    void createTransaction(TransactionDto transactionDto, Long userId);

    void cancelTransaction(Long txId);

    boolean completeTransaction(Long txId,Long userId);

    void cancelMultipleTransactions(List<Long> txIds);

    LinkedHashSet<TransactionDto> getTransactionSet(int pageNumber, int pageSize);

}
