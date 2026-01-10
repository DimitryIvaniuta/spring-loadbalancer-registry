package com.github.dimitryivaniuta.loadbalancer.service;

import com.github.dimitryivaniuta.loadbalancer.TestApp;
import com.github.dimitryivaniuta.loadbalancer.api.LoadBalancer;
import com.github.dimitryivaniuta.loadbalancer.exceptions.CapacityExceededException;
import com.github.dimitryivaniuta.loadbalancer.exceptions.DuplicateAddressException;
import com.github.dimitryivaniuta.loadbalancer.repo.LbDecisionRepository;
import com.github.dimitryivaniuta.loadbalancer.repo.LbInstanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest(classes = TestApp.class)
class LoadBalancerServiceIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("lb")
            .withUsername("lb")
            .withPassword("lb");

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        r.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired
    LoadBalancer loadBalancer; // autowire the real Spring bean (so @Lookup works)

    @Autowired
    LbInstanceRepository instanceRepo;

    @Autowired
    LbDecisionRepository decisionRepo;

    @BeforeEach
    void cleanDb() {
        // keep tests isolated
        decisionRepo.deleteAll();
        instanceRepo.deleteAll();
    }

    @Test
    void registerDuplicateIsRejected() {
        loadBalancer.register(" http://a ");
        assertThatThrownBy(() -> loadBalancer.register("http://a"))
                .isInstanceOf(DuplicateAddressException.class);
    }

    @Test
    void capacityUpTo10() {
        for (int i = 0; i < 10; i++) loadBalancer.register("http://n" + i);

        assertThatThrownBy(() -> loadBalancer.register("http://overflow"))
                .isInstanceOf(CapacityExceededException.class);
    }

    @Test
    void roundRobinNext() {
        loadBalancer.register("http://a");
        loadBalancer.register("http://b");
        loadBalancer.register("http://c");

        assertThat(loadBalancer.nextAddress()).contains("http://a");
        assertThat(loadBalancer.nextAddress()).contains("http://b");
        assertThat(loadBalancer.nextAddress()).contains("http://c");
        assertThat(loadBalancer.nextAddress()).contains("http://a");
    }
}
