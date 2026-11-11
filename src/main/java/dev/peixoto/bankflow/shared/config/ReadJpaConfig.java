package dev.peixoto.bankflow.shared.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;


@Configuration
@EnableJpaRepositories(
        basePackages = {
                "dev.peixoto.bankflow.customer.repository.read",
                "dev.peixoto.bankflow.account.repository.read",
                "dev.peixoto.bankflow.invoice.repository.read",
                "dev.peixoto.bankflow.payment.repository.read"
        },
        entityManagerFactoryRef = "readEntityManagerFactory",
        transactionManagerRef = "readTransactionManager")
public class ReadJpaConfig {

    @Bean
    @DependsOn("readFlyway")
    public LocalContainerEntityManagerFactoryBean readEntityManagerFactory(
            @Qualifier("readDataSource") DataSource dataSource) {
        return JpaFactories.create(dataSource, "read",
                "dev.peixoto.bankflow.customer.query",
                "dev.peixoto.bankflow.account.query",
                "dev.peixoto.bankflow.invoice.query",
                "dev.peixoto.bankflow.payment.query");
    }

    @Bean
    public PlatformTransactionManager readTransactionManager(
            @Qualifier("readEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
