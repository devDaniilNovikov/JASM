package dn.jasm.controller;

import dn.jasm.dto.transaction.TransactionDto;
import dn.jasm.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private static final String CREATE_TRANSACTION = "/api/v1/tx/create";
    private static final String CANCEL_TRANSACTION = "/api/v1/tx/cancel";
    private static final String CANCEL_MULTIPLE_TRANSACTIONS = "/api/v1/txs/cancel";

    private final TransactionService transactionService;


    @PostMapping(CREATE_TRANSACTION)
    public void createTransaction(@RequestBody TransactionDto transactionDto, @RequestParam Long userId){
        transactionService.createTransaction(transactionDto, userId);
    }

    @PatchMapping(CANCEL_TRANSACTION)
    public void cancelTransaction(@RequestParam Long txId){
         transactionService.cancelTransaction(txId);
    }

    @PatchMapping(CANCEL_MULTIPLE_TRANSACTIONS)
    public void cancelTransactions(@RequestParam List<Long> txIds){
        transactionService.cancelMultipleTransactions(txIds);
    }


}
