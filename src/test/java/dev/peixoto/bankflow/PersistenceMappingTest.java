package dev.peixoto.bankflow;

import dev.peixoto.bankflow.account.entity.Account;
import dev.peixoto.bankflow.account.entity.AccountStatus;
import dev.peixoto.bankflow.account.query.AccountBalanceView;
import dev.peixoto.bankflow.account.repository.read.AccountReadRepository;
import dev.peixoto.bankflow.account.repository.write.AccountWriteRepository;
import dev.peixoto.bankflow.customer.entity.Customer;
import dev.peixoto.bankflow.customer.repository.write.CustomerWriteRepository;
import dev.peixoto.bankflow.outbox.entity.OutboxEvent;
import dev.peixoto.bankflow.outbox.repository.write.OutboxEventWriteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PersistenceMappingTest {

    @Autowired CustomerWriteRepository customers;
    @Autowired AccountWriteRepository accounts;
    @Autowired OutboxEventWriteRepository outbox;
    @Autowired AccountReadRepository accountViews;

    @Test
    void writeAndReadSidesPersistIndependently() {
        var customer = new Customer();
        customer.setFullName("Maria Silva");
        customer.setEmail("maria@example.com");
        customer.setDocumentNumber("12345678900");
        customers.save(customer);

        var account = new Account();
        account.setCustomer(customer);
        account.setAccountNumber("0001-1");
        account.setBalance(new BigDecimal("100.00"));
        account.setStatus(AccountStatus.ACTIVE);
        var saved = accounts.saveAndFlush(account);
        assertThat(saved.getVersion()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        var event = new OutboxEvent();
        event.setAggregateId(saved.getId());
        event.setAggregateType("Account");
        event.setEventType("AccountCreated");
        event.setPayload("{\"accountNumber\":\"0001-1\"}");
        assertThat(outbox.saveAndFlush(event).getCreatedAt()).isNotNull();

        var view = new AccountBalanceView(saved.getId(), customer.getId(), "0001-1",
                new BigDecimal("100.00"), "ACTIVE", OffsetDateTime.now());
        accountViews.saveAndFlush(view);
        assertThat(accountViews.findByCustomerId(customer.getId())).hasSize(1);
        assertThat(accountViews.findById(UUID.randomUUID())).isEmpty();
    }
}
