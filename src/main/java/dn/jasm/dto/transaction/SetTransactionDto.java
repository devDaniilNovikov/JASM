package dn.jasm.dto.transaction;

import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class SetTransactionDto {

    private Set<TransactionDto> transactions = new LinkedHashSet<>();
}
