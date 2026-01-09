package com.ctrlf.infra.keycloak.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ConditionalOnProperty(name = "spring.keycloak.datasource.url")
@EnableJpaRepositories(
    basePackages = "com.ctrlf.infra.keycloak.repository",
    entityManagerFactoryRef = "keycloakEntityManagerFactory",
    transactionManagerRef = "keycloakTransactionManager"
)
public class KeycloakDataSourceConfig {

    /**
     * Keycloak DB DataSource
     * Keycloak DB는 별도 컨테이너(keycloak-postgres)에 있음
     * Flyway가 이 DataSource를 사용하지 않도록 조건부 생성
     */
    @Bean(name = "keycloakDataSource")
    @org.springframework.context.annotation.Lazy
    public DataSource keycloakDataSource(
            @Value("${spring.keycloak.datasource.url}") String url,
            @Value("${spring.keycloak.datasource.username}") String username,
            @Value("${spring.keycloak.datasource.password}") String password,
            @Value("${spring.keycloak.datasource.driver-class-name:org.postgresql.Driver}") String driverClassName) {
        return DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .url(url)
            .username(username)
            .password(password)
            .driverClassName(driverClassName)
            .build();
    }

    /**
     * Keycloak DB EntityManagerFactory
     */
    @Bean(name = "keycloakEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean keycloakEntityManagerFactory(
            @Qualifier("keycloakDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.ctrlf.infra.keycloak.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setPersistenceUnitName("keycloak");
        
        java.util.Properties properties = new java.util.Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.hbm2ddl.auto", "none");
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.default_schema", "public");
        em.setJpaProperties(properties);
        
        return em;
    }

    /**
     * Keycloak DB TransactionManager
     */
    @Bean(name = "keycloakTransactionManager")
    public PlatformTransactionManager keycloakTransactionManager(
            @Qualifier("keycloakEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

