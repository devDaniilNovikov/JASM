package dn.jasm.repository;

import dn.jasm.entity.ShopEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Map;

public interface ShopRepository extends JpaRepository<ShopEntity, Long> {

}