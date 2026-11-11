package dev.peixoto.bankflow.shared.config;

import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Map;

final class JpaFactories {

    private JpaFactories() {
    }

    static LocalContainerEntityManagerFactoryBean create(DataSource dataSource, String unitName, String... entityPackages) {
        var emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPersistenceUnitName(unitName);
        emf.setPackagesToScan(entityPackages);
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setJpaPropertyMap(Map.of(
                "hibernate.hbm2ddl.auto", "validate",
                "hibernate.format_sql", "true"));
        return emf;
    }
}
