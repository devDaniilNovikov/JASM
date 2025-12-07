package dn.jasm.repository;

import dn.jasm.entity.CommentEntity;
import dn.jasm.mapper.OrderMapper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {


    List<CommentEntity> findAllByUserId(Long userId);
}