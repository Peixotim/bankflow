package dev.peixoto.bankflow.account.repository.write;

import dev.peixoto.bankflow.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;


public interface AccountWriteRepository extends JpaRepository<Account, UUID> {
    boolean existsByAccountNumber(String accountNumber);
}
