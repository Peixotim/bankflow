package dev.peixoto.bankflow.account.repository.read;

import dev.peixoto.bankflow.account.query.AccountBalanceView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountReadRepository extends JpaRepository<AccountBalanceView, UUID> {

    List<AccountBalanceView> findByCustomerId(UUID customerId);
}
