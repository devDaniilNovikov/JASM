package dn.jasm.repository;

import dn.jasm.entity.CardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<CardEntity,Long> {

    Optional<CardEntity> findByCardNumber(String cardNumber);

    List<CardEntity> findByUserId(Long userId);

    @Query(value = "SELECT c FROM CardEntity c WHERE c.user.id = :userId")
    List<CardEntity> findAllByUserId(@Param("userId") Long userId);


    CardEntity findByPaymentEntity_Id(String paymentEntityId);

}
