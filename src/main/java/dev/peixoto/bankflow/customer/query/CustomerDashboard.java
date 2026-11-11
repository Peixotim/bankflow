package dev.peixoto.bankflow.customer.query;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer_dashboard")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDashboard {

    @Id
    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(name = "total_accounts", nullable = false)
    private int totalAccounts;

    @Column(name = "total_cards", nullable = false)
    private int totalCards;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
