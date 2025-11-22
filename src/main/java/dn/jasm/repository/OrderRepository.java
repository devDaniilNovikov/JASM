package dn.jasm.repository;

import dn.jasm.entity.OrderEntity;
import dn.jasm.entity.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query("select o from OrderEntity o join fetch o.user where o.user.id = :userId")
    Optional<OrderEntity> findByUserId(Long userId);

    List<OrderEntity> findAllByUserId(Long userId);

    List<OrderEntity> findAllByOrderStatus(OrderStatus orderStatus, Pageable pageable);

}