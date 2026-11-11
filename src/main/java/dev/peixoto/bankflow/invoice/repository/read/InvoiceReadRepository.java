package dev.peixoto.bankflow.invoice.repository.read;

import dev.peixoto.bankflow.invoice.query.CardInvoiceView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceReadRepository extends JpaRepository<CardInvoiceView, UUID> {

    List<CardInvoiceView> findByCustomerId(UUID customerId);
}
