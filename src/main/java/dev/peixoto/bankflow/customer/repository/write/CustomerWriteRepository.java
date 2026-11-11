package dev.peixoto.bankflow.customer.repository.write;

import dev.peixoto.bankflow.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;


public interface CustomerWriteRepository extends JpaRepository<Customer, UUID> {
    boolean existsByEmail(String email);

    boolean existsByDocumentNumber(String documentNumber);
}
