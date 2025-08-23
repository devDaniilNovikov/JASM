package dn.jasm.dto.transaction;

import lombok.Builder;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;

@Data
@Builder
public class TransactionDto {

    private Long userId;
    private Long txId;
    @Nullable
    private Long paymentId;
    private BigDecimal balance;
    private Long orderId;
    private Boolean completedAt;
}
