package dev.peixoto.bankflow.payment.repository.write;

import dev.peixoto.bankflow.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface PaymentWriteRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}
