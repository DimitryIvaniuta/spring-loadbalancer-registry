package com.github.dimitryivaniuta.loadbalancer.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoE2EIT {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17.7-alpine")
            .withDatabaseName("lb")
            .withUsername("lb")
            .withPassword("lb");

    static { POSTGRES.start(); }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        r.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        r.add("spring.flyway.enabled", () -> "true");
    }

    @org.springframework.beans.factory.annotation.Autowired
    TestRestTemplate rest;

    @Test
    void registerAndNextWorks() {
        var id = rest.postForObject("/api/lb/instances", java.util.Map.of("address", "http://a"), Long.class);
        assertThat(id).isNotNull();

        var next = rest.getForEntity("/api/lb/instances/next", String.class);
        assertThat(next.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(next.getBody()).contains("http://a");
    }
}
