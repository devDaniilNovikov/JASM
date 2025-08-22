package dn.jasm.repository;

import dn.jasm.entity.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CardRepository extends JpaRepository<CardEntity,Long> {

    @Query(value = "SELECT c FROM CardEntity c JOIN FETCH c.user u WHERE c.cardNumber = :cardNumber")

    CardEntity findByCardNumber(Integer cardNumber);


}
