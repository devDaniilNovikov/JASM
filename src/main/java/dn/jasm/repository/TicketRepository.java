package dn.jasm.repository;

import dn.jasm.entity.TicketEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


public interface TicketRepository extends CrudRepository<TicketEntity,String> {
}
