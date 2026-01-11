package com.github.dimitryivaniuta.loadbalancer.strategy;

import com.github.dimitryivaniuta.loadbalancer.TestApp;
import com.github.dimitryivaniuta.loadbalancer.api.LoadBalancer;
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

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(
        classes = TestApp.class,
        properties = {
                "loadbalancer.strategy=LEAST_RECENTLY_USED",
                "loadbalancer.max-instances=10"
        }
)
class LeastRecentlyUsedStrategyIT {

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

        // keep schema in sync with Flyway migrations
        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    LoadBalancer lb;

    @Autowired
    LbInstanceRepository instanceRepo;

    @Autowired
    ObjectProvider<LbDecisionRepository> decisionRepoProvider;

    @BeforeEach
    void cleanDb() {
        var decisionRepo = decisionRepoProvider.getIfAvailable();
        if (decisionRepo != null) decisionRepo.deleteAll();
        instanceRepo.deleteAll();
    }

    @Test
    void lruPicksNeverUsedFirst_thenOldestUsed_perTenantAndGroup() {
        lb.register(TENANT, GROUP, "http://a");
        lb.register(TENANT, GROUP, "http://b");
        lb.register(TENANT, GROUP, "http://c");

        // Initially last_used_at is NULL for all => order by (NULLS FIRST, id ASC)
        assertThat(lb.nextAddress(TENANT, GROUP)).contains("http://a");
        assertThat(lb.nextAddress(TENANT, GROUP)).contains("http://b");
        assertThat(lb.nextAddress(TENANT, GROUP)).contains("http://c");

        // Now all have last_used_at set; the oldest is the one used first => "a"
        assertThat(lb.nextAddress(TENANT, GROUP)).contains("http://a");
    }

    @Test
    void lruIsIsolatedBetweenGroupsAndTenants() {
        lb.register(TENANT, GROUP, "http://a");
        lb.register(TENANT, "shipping", "http://a");
        lb.register("tenant-2", GROUP, "http://a");

        // each (tenant, group) has its own LRU sequence
        assertThat(lb.nextAddress(TENANT, GROUP)).contains("http://a");
        assertThat(lb.nextAddress(TENANT, "shipping")).contains("http://a");
        assertThat(lb.nextAddress("tenant-2", GROUP)).contains("http://a");
    }
}
