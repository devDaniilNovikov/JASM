package dn.jasm.dto.transaction;

import dn.jasm.entity.enums.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;

@Data
@Builder
public class TransactionDto {

    @Schema(name = "txId", description = "Уникальный идентификатор транзакции")
    private Long txId;

    @Schema(name = "userId", description = "Уникальный идентификатор пользователя совершающего транзакцию")
    private Long userId;

    @Schema(name = "cardId", description = "Уникальный идентификатор карты , с которой совершается транзакция")
    private Long cardId;

    @Schema(name = "amount", description = "Суммарная стоимость заказа в транзакции")
    private BigDecimal amount;

    @Schema(name = "orderId", description = "Уникальный идентификатор заказа в транзакции")
    private Long orderId;

    @Schema(name = "completedAt", description = "Возможность совершения транзакции")
    private Boolean completedAt;

    @Schema(name = "transactionStatus", description = "Статус транзакции")
    private TransactionStatus transactionStatus;
}
