package com.github.dimitryivaniuta.loadbalancer.strategy;

import com.github.dimitryivaniuta.loadbalancer.TestApp;
import com.github.dimitryivaniuta.loadbalancer.api.LoadBalancer;
import com.github.dimitryivaniuta.loadbalancer.infra.AbstractPostgresIT;
import com.github.dimitryivaniuta.loadbalancer.repo.LbDecisionRepository;
import com.github.dimitryivaniuta.loadbalancer.repo.LbInstanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class LeastRecentlyUsedStrategyIT extends AbstractPostgresIT {

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
        lb.register(SCOPE1, "http://a");
        lb.register(SCOPE1, "http://b");
        lb.register(SCOPE1, "http://c");

        // Initially last_used_at is NULL for all => order by (NULLS FIRST, id ASC)
        assertThat(lb.nextAddress(SCOPE1)).contains("http://a");
        assertThat(lb.nextAddress(SCOPE1)).contains("http://b");
        assertThat(lb.nextAddress(SCOPE1)).contains("http://c");

        // Now all have last_used_at set; the oldest is the one used first => "a"
        assertThat(lb.nextAddress(SCOPE1)).contains("http://a");
    }

    @Test
    void lruIsIsolatedBetweenGroupsAndTenants() {
        lb.register(SCOPE1, "http://a");
        lb.register(SCOPE2, "http://a");
        lb.register(SCOPE3, "http://a");

        // each (tenant, group) has its own LRU sequence
        assertThat(lb.nextAddress(SCOPE1)).contains("http://a");
        assertThat(lb.nextAddress(SCOPE2)).contains("http://a");
        assertThat(lb.nextAddress(SCOPE3)).contains("http://a");
    }
}
