package dn.jasm.controller;

import dn.jasm.configuration.swagger.transaction.SwaggerAnnotationForTransaction;
import dn.jasm.configuration.swagger.transaction.SwaggerAnnotationForTransactionCollection;
import dn.jasm.dto.transaction.TransactionDto;
import dn.jasm.service.TransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Tag(name = "Transaction",description = "Действия с транзакциями")
public class TransactionController {

    private static final String CREATE_TRANSACTION = "/api/v1/tx/create";
    private static final String CANCEL_TRANSACTION = "/api/v1/tx/cancel";
    private static final String CANCEL_MULTIPLE_TRANSACTIONS = "/api/v1/txs/cancel";
    private static final String GET_TRANSACTION_SET = "/api/v1/tx/all";

    private final TransactionService transactionService;


    @PostMapping(value = CREATE_TRANSACTION,produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE)
    @SwaggerAnnotationForTransaction(operation = "Создание транзакции")
    public void createTransaction(@RequestBody TransactionDto transactionDto, @RequestParam Long userId){
        transactionService.createTransaction(transactionDto, userId);
    }

    @PatchMapping(value = CANCEL_TRANSACTION,produces = MediaType.APPLICATION_JSON_VALUE)
    @SwaggerAnnotationForTransaction(operation = "Отмена транзакции")
    public void cancelTransaction(@RequestParam Long txId){
         transactionService.cancelTransaction(txId);
    }

    @PatchMapping(value = CANCEL_MULTIPLE_TRANSACTIONS,produces = MediaType.APPLICATION_JSON_VALUE)
    @SwaggerAnnotationForTransactionCollection(operation = "Отмена нескольких транзакций")
    public void cancelTransactions(@RequestParam List<Long> txIds){
        transactionService.cancelMultipleTransactions(txIds);
    }

    @GetMapping(value = GET_TRANSACTION_SET,produces = MediaType.APPLICATION_JSON_VALUE)
    @SwaggerAnnotationForTransactionCollection(operation = "Получение транзакций постранично")
    public LinkedHashSet<TransactionDto> findAllWithPagination(@RequestParam int pageNumber,
                                                               @RequestParam int pageSize){
        return transactionService.getTransactionSet(pageNumber, pageSize);
    }


}
