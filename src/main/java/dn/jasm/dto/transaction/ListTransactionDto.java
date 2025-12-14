package dn.jasm.dto.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(name = "ListTransaction", description = "Список транзакций")
public class ListTransactionDto {

    @Schema(name = "transactions", description = "Список запрашиваемых транзакций")
    private List<TransactionDto> transactions = new ArrayList<>();
}
