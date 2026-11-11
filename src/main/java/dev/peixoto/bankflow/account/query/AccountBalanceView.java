package dev.peixoto.bankflow.account.query;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_balance_view")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceView {

    @Id
    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "account_number", nullable = false, length = 30)
    private String accountNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "account_status", nullable = false, length = 20)
    private String accountStatus;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
