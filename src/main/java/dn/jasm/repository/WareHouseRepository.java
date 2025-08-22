package dn.jasm.repository;

import dn.jasm.entity.WareHouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WareHouseRepository extends JpaRepository<WareHouseEntity, Long> {
}