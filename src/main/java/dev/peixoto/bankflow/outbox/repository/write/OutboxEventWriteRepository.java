package dev.peixoto.bankflow.outbox.repository.write;

import dev.peixoto.bankflow.outbox.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;


public interface OutboxEventWriteRepository extends JpaRepository<OutboxEvent, UUID> {

}
