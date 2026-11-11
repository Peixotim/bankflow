package dev.peixoto.bankflow.card.repository.write;

import dev.peixoto.bankflow.card.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;


public interface CardWriteRepository extends JpaRepository<Card, UUID> {

}
