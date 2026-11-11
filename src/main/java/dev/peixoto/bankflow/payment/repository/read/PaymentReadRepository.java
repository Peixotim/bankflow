package dev.peixoto.bankflow.payment.repository.read;

import dev.peixoto.bankflow.payment.query.PaymentHistoryView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentReadRepository extends JpaRepository<PaymentHistoryView, UUID> {

    List<PaymentHistoryView> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
