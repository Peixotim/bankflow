package dev.peixoto.bankflow.shared.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;


@Configuration
@EnableJpaRepositories(
        basePackages = {
                "dev.peixoto.bankflow.customer.repository.write",
                "dev.peixoto.bankflow.account.repository.write",
                "dev.peixoto.bankflow.card.repository.write",
                "dev.peixoto.bankflow.invoice.repository.write",
                "dev.peixoto.bankflow.payment.repository.write",
                "dev.peixoto.bankflow.ledger.repository.write",
                "dev.peixoto.bankflow.outbox.repository.write"
        },
        entityManagerFactoryRef = "writeEntityManagerFactory",
        transactionManagerRef = "writeTransactionManager")
public class WriteJpaConfig {

    @Bean
    @Primary
    @DependsOn("writeFlyway")
    public LocalContainerEntityManagerFactoryBean writeEntityManagerFactory(
            @Qualifier("writeDataSource") DataSource dataSource) {
        return JpaFactories.create(dataSource, "write",
                "dev.peixoto.bankflow.customer.entity",
                "dev.peixoto.bankflow.account.entity",
                "dev.peixoto.bankflow.card.entity",
                "dev.peixoto.bankflow.invoice.entity",
                "dev.peixoto.bankflow.payment.entity",
                "dev.peixoto.bankflow.ledger.entity",
                "dev.peixoto.bankflow.outbox.entity");
    }

    @Bean
    @Primary
    public PlatformTransactionManager writeTransactionManager(
            @Qualifier("writeEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
