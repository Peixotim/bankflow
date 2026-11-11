
package com.bankflow.shared.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway writeFlyway(
            @Value("${spring.datasource.write.url}") String url,
            @Value("${spring.datasource.write.username}") String username,
            @Value("${spring.datasource.write.password}") String password
    ) {
        return Flyway.configure()
                .dataSource(url, username, password)
                .locations("classpath:db/migration/write")
                .load();
    }

    @Bean(initMethod = "migrate")
    public Flyway readFlyway(
            @Value("${spring.datasource.read.url}") String url,
            @Value("${spring.datasource.read.username}") String username,
            @Value("${spring.datasource.read.password}") String password
    ) {
        return Flyway.configure()
                .dataSource(url, username, password)
                .locations("classpath:db/migration/read")
                .load();
    }
}
