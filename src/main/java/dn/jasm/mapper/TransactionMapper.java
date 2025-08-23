package dn.jasm.mapper;

import dn.jasm.dto.transaction.TransactionDto;
import dn.jasm.entity.OrderEntity;
import dn.jasm.entity.TransactionEntity;
import dn.jasm.entity.UserEntity;
import dn.jasm.entity.enums.TransactionStatus;
import dn.jasm.repository.OrderRepository;
import dn.jasm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class TransactionMapper {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;



    public TransactionEntity mapToEntity(TransactionDto transactionDto,
                                         UserEntity user,
                                         OrderEntity order){
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setId(transactionDto.getTxId());
        transactionEntity.setCompletedAt(true);
        transactionEntity.setUserEntity(user);
        transactionEntity.setOrderEntity(order);
        transactionEntity.setTransactionStatus(TransactionStatus.COMPLETED);
        return transactionEntity;
    }

    public TransactionDto mapToDto(TransactionEntity transactionEntity){
        return TransactionDto.builder()
                .txId(transactionEntity.getId())
                .userId(transactionEntity.getUserEntity().getId())
                .orderId(transactionEntity.getOrderEntity().getId())
                .balance(transactionEntity.getOrderEntity().getAmount())
                .completedAt(true)
                .build();
    }

    public List<TransactionDto> mapToDtoList(List<TransactionEntity> txEntities){
        return txEntities.stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<TransactionEntity> mapToEntityList(List<TransactionDto> transactionDtos){
        return transactionDtos.stream()
                .map(this::mapToEntityWithoutUserAndOrder)
                .toList();
    }

    private TransactionEntity mapToEntityWithoutUserAndOrder(TransactionDto transactionDto){
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setId(transactionDto.getTxId());
        var user = userRepository.findById(transactionDto.getUserId()).orElseThrow(RuntimeException::new);
        var order = orderRepository.findById(transactionDto.getOrderId()).orElseThrow(RuntimeException::new);
        transactionEntity.setUserEntity(user);
        transactionEntity.setOrderEntity(order);
        transactionEntity.setCompletedAt(true);
        transactionEntity.setTransactionStatus(TransactionStatus.COMPLETED);
        return transactionEntity;
    }

    public String mapToString(TransactionEntity tx){
        return String.valueOf(tx);
    }

}
