package dn.jasm.mapper;

import dn.jasm.dto.transaction.ListTransactionDto;
import dn.jasm.dto.transaction.SetTransactionDto;
import dn.jasm.dto.transaction.TransactionDto;
import dn.jasm.entity.TransactionEntity;
import dn.jasm.entity.enums.TransactionStatus;
import dn.jasm.repository.OrderRepository;
import dn.jasm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransactionMapper {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public TransactionDto mapToDto(TransactionEntity transactionEntity){
        return TransactionDto.builder()
                .txId(Long.valueOf(String.valueOf(transactionEntity.getId())))
                .userId(transactionEntity.getUser().getId())
                .orderId(transactionEntity.getOrderEntity().getId())
                .cardId(transactionEntity.getCard().getId())
                .amount(transactionEntity.getOrderEntity().getAmount())
                .transactionStatus(TransactionStatus.COMPLETED)
                .completedAt(true)
                .build();
    }

    public ListTransactionDto mapToDtoList(List<TransactionEntity> txEntities){
        ListTransactionDto listTransactionDto = new ListTransactionDto();
        listTransactionDto.setTransactions(txEntities.stream()
                .map(this::mapToDto)
                .filter(Objects::nonNull)
                .toList());
        return listTransactionDto;
    }

    public List<TransactionEntity> mapToEntityList(List<TransactionDto> transactionDtoList){
        return transactionDtoList.stream()
                .filter(Objects::nonNull)
                .map(this::mapToEntityWithoutUserAndOrder)
                .toList();
    }

    public TransactionEntity mapToEntityWithoutUserAndOrder(TransactionDto transactionDto){
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setId(transactionDto.getTxId());
        var user = userRepository.findById(transactionDto.getUserId())
                .orElseThrow(RuntimeException::new);
        var order = orderRepository.findById(transactionDto.getOrderId())
                .orElseThrow(RuntimeException::new);
        transactionEntity.setUser(user);
        transactionEntity.setOrderEntity(order);
        transactionEntity.setCompletedAt(true);
        transactionEntity.setTransactionStatus(TransactionStatus.COMPLETED);
        return transactionEntity;
    }

    public String mapToString(TransactionEntity tx){
        return String.valueOf(tx);
    }

    public SetTransactionDto mapToDtoSet(Set<TransactionEntity> transactions){
        SetTransactionDto setTransactionDto = new SetTransactionDto();
        setTransactionDto.setTransactions(transactions.stream()
                .map(this::mapToDto)
                .sorted(Comparator.comparing(TransactionDto::getUserId)
                .thenComparing(tx->tx.getTransactionStatus().equals(TransactionStatus.COMPLETED))
                .thenComparing(TransactionDto::getCardId))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        return setTransactionDto;
    }

    public LinkedHashSet<TransactionEntity> mapToEntitySet(Set<TransactionDto> transactions){
        return transactions.stream()
                .map(this::mapToEntityWithoutUserAndOrder)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
