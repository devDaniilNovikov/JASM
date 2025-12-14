package dn.jasm.dto.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Schema(name = "SetTransaction", description = "Множество  транзакций")
public class SetTransactionDto {

    @Schema(name = "transactions", description = "Множество запрашиваемых транзакций")
    private Set<TransactionDto> transactions = new LinkedHashSet<>();
}
