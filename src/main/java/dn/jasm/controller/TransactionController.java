package dn.jasm.controller;

import dn.jasm.configuration.swagger.transaction.SwaggerAnnotationForTransaction;
import dn.jasm.configuration.swagger.transaction.SwaggerAnnotationForTransactionCollection;
import dn.jasm.dto.transaction.SetTransactionDto;
import dn.jasm.dto.transaction.TransactionDto;
import dn.jasm.service.TransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Transaction",description = "Действия с транзакциями")
public class TransactionController {

    private static final String CREATE_TRANSACTION = "/api/v1/tx/create";
    private static final String CANCEL_TRANSACTION = "/api/v1/tx/cancel";
    private static final String CANCEL_MULTIPLE_TRANSACTIONS = "/api/v1/txs/cancel";
    private static final String GET_TRANSACTION_SET = "/api/v1/tx/all";
    private static final String DELETE_TRANSACTION = "/api/v1/tx/delete";
    private static final String GET_TRANSACTION_BY_ID = "/api/v1/tx/get/{id}";

    private final TransactionService transactionService;

    @DeleteMapping(DELETE_TRANSACTION)
    @SwaggerAnnotationForTransaction(operation = "Удаление транзакции по ее уникальному идентификатору")
    public void deleteTransaction(@RequestParam Long txId){
        transactionService.deleteTransaction(txId);
    }

    @GetMapping(GET_TRANSACTION_BY_ID)
    public TransactionDto getById(@PathVariable Long id){
        return transactionService.getTransactionById(id);
    }


    @PostMapping(CREATE_TRANSACTION)
    @SwaggerAnnotationForTransaction(operation = "Создание транзакции")
    public void createTransaction(@RequestParam Long userId,
                                  @RequestParam Long orderId,
                                  @RequestParam Long cardId){
        transactionService.createTransaction(userId, orderId,cardId);
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
    public SetTransactionDto findAllWithPagination(@RequestParam int pageNumber,
                                                   @RequestParam int pageSize){
        return transactionService.getTransactionSet(pageNumber, pageSize);
    }



}
