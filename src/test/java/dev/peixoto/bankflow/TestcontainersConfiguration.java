package dev.peixoto.bankflow;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka-native:latest"));
    }

    @Bean
    PostgreSQLContainer postgresWrite() {
        return new PostgreSQLContainer("postgres:17-alpine").withDatabaseName("bankflow_write");
    }

    @Bean
    PostgreSQLContainer postgresRead() {
        return new PostgreSQLContainer("postgres:17-alpine").withDatabaseName("bankflow_read");
    }

    @Bean
    DynamicPropertyRegistrar databaseProperties(PostgreSQLContainer postgresWrite, PostgreSQLContainer postgresRead) {
        return registry -> {
            registry.add("spring.datasource.write.url", postgresWrite::getJdbcUrl);
            registry.add("spring.datasource.write.username", postgresWrite::getUsername);
            registry.add("spring.datasource.write.password", postgresWrite::getPassword);
            registry.add("spring.datasource.read.url", postgresRead::getJdbcUrl);
            registry.add("spring.datasource.read.username", postgresRead::getUsername);
            registry.add("spring.datasource.read.password", postgresRead::getPassword);
        };
    }

}
