package dn.jasm.repository;

import dn.jasm.entity.StorageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageRepository extends JpaRepository<StorageEntity, Long> {
}