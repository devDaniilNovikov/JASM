package dn.jasm.dto.transaction;

import dn.jasm.entity.enums.TransactionStatus;
import lombok.Builder;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;

@Data
@Builder
public class TransactionDto {

    private Long txId;
    private Long userId;
    private Long cardId;
    private BigDecimal amount;
    private Long orderId;
    private Boolean completedAt;
    private TransactionStatus transactionStatus;
}
