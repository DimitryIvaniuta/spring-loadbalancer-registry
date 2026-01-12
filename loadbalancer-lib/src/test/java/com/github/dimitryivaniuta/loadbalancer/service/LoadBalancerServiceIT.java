package com.github.dimitryivaniuta.loadbalancer.service;

import com.github.dimitryivaniuta.loadbalancer.TestApp;
import com.github.dimitryivaniuta.loadbalancer.api.LoadBalancer;
import com.github.dimitryivaniuta.loadbalancer.exceptions.CapacityExceededException;
import com.github.dimitryivaniuta.loadbalancer.exceptions.DuplicateAddressException;
import com.github.dimitryivaniuta.loadbalancer.repo.LbDecisionRepository;
import com.github.dimitryivaniuta.loadbalancer.repo.LbInstanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest(
        classes = TestApp.class,
        properties = {
                // keep deterministic and simple for this IT
                "loadbalancer.strategy=ROUND_ROBIN",
                "loadbalancer.max-instances=10"
        }
)
class LoadBalancerServiceIT {

    private static final String TENANT = "tenant-1";
    private static final String GROUP = "payments";

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

        // ensure Flyway runs (if you use validate, keep schema in sync with migrations)
        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    LoadBalancer loadBalancer;

    @Autowired
    LbInstanceRepository instanceRepo;

    @Autowired
    ObjectProvider<LbDecisionRepository> decisionRepoProvider;

    @BeforeEach
    void cleanDb() {
        // decisions table may be optional in your current branch; don’t fail if repo not present
        var decisionRepo = decisionRepoProvider.getIfAvailable();
        if (decisionRepo != null) {
            decisionRepo.deleteAll();
        }
        instanceRepo.deleteAll();
    }

    @Test
    void registerDuplicateIsRejected_perTenantAndGroup() {
        loadBalancer.register(TENANT, GROUP, " http://a ");

        assertThatThrownBy(() -> loadBalancer.register(TENANT, GROUP, "http://a"))
                .isInstanceOf(DuplicateAddressException.class);

        // same address is allowed in different group or tenant (by design)
        assertThatCode(() -> loadBalancer.register(TENANT, "shipping", "http://a")).doesNotThrowAnyException();
        assertThatCode(() -> loadBalancer.register("tenant-2", GROUP, "http://a")).doesNotThrowAnyException();
    }

    @Test
    void capacityUpTo10_isPerTenantAndGroup() {
        for (int i = 0; i < 10; i++) {
            loadBalancer.register(TENANT, GROUP, "http://n" + i);
        }

        assertThatThrownBy(() -> loadBalancer.register(TENANT, GROUP, "http://overflow"))
                .isInstanceOf(CapacityExceededException.class);

        // another group has its own capacity bucket
        assertThatCode(() -> loadBalancer.register(TENANT, "shipping", "http://overflow"))
                .doesNotThrowAnyException();
    }

    @Test
    void roundRobinNext_isPerTenantAndGroup() {
        loadBalancer.register(TENANT, GROUP, "http://a");
        loadBalancer.register(TENANT, GROUP, "http://b");
        loadBalancer.register(TENANT, GROUP, "http://c");

        assertThat(loadBalancer.nextAddress(TENANT, GROUP)).contains("http://a");
        assertThat(loadBalancer.nextAddress(TENANT, GROUP)).contains("http://b");
        assertThat(loadBalancer.nextAddress(TENANT, GROUP)).contains("http://c");
        assertThat(loadBalancer.nextAddress(TENANT, GROUP)).contains("http://a");

        // different group is independent
        loadBalancer.register(TENANT, "shipping", "http://s1");
        assertThat(loadBalancer.nextAddress(TENANT, "shipping")).contains("http://s1");
    }
}
