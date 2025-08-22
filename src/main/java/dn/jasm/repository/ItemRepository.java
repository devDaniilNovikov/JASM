package dn.jasm.repository;

import dn.jasm.entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> {


    @Query("SELECT item from ItemEntity item where item.order.id = :orderId")
    List<ItemEntity> findAllByOrderId(@Param("orderId") Long orderId);

    List<ItemEntity> findAllByIdIn(List<Long> ids);
}