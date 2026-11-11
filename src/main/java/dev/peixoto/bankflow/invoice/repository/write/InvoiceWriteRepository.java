package dev.peixoto.bankflow.invoice.repository.write;

import dev.peixoto.bankflow.invoice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.time.LocalDate;
import java.util.Optional;

public interface InvoiceWriteRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByCardIdAndReferenceMonth(UUID cardId, LocalDate referenceMonth);
}
