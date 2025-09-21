package dn.jasm.dto.transaction;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ListTransactionDto {

    private List<TransactionDto> transactions = new ArrayList<>();
}
