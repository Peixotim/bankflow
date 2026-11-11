package dev.peixoto.bankflow.customer.repository.read;

import dev.peixoto.bankflow.customer.query.CustomerDashboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerReadRepository extends JpaRepository<CustomerDashboard, UUID> {
}
