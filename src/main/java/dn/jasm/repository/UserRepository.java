package dn.jasm.repository;
import dn.jasm.entity.UserEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.scheduling.annotation.Async;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @EntityGraph(attributePaths = {"orders,cards,comments"})
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);


    @EntityGraph(attributePaths = "comments")
    Optional<UserEntity> findById(Long id, PageRequest pageRequest);

    @EntityGraph(attributePaths = {"orders,cards,comments"})
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);

    boolean existsByUsernameOrPhoneNumber(String username, String phoneNumber);

    List<UserEntity> findAllByStatus(String status);

    List<UserEntity> getAllByCountOfDealsNotNull();

    Optional<UserEntity> findByTransactionEntityId(Long txId);



    boolean existsByEmail(String email);

    @Modifying
    @Query(value = "UPDATE jasm.user SET balance = :value WHERE id = :id",nativeQuery = true)
    void updateBalanceOfUser(Long id, BigDecimal value);

}