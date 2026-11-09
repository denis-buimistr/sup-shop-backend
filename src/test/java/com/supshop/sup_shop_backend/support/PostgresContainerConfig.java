package com.supshop.sup_shop_backend.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

// настоящий Postgres в Docker вместо локальной базы. @ServiceConnection сам подставляет
// адрес, логин и пароль контейнера в datasource, а Flyway накатывает на него все миграции
@TestConfiguration(proxyBeanMethods = false)
public class PostgresContainerConfig {

    // тот же образ, что у локальной базы, чтобы тесты проверяли ту же версию Postgres
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:16");
    }
}
