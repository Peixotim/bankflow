package dev.peixoto.bankflow.ledger.repository.write;

import dev.peixoto.bankflow.ledger.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;


public interface LedgerEntryWriteRepository extends JpaRepository<LedgerEntry, UUID> {

}
