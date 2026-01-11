package com.github.dimitryivaniuta.loadbalancer.strategy;

import com.github.dimitryivaniuta.loadbalancer.api.ContextAwareStrategy;
import com.github.dimitryivaniuta.loadbalancer.api.LoadBalancingStrategy;
import com.github.dimitryivaniuta.loadbalancer.repo.LbInstanceRepository;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public class LeastRecentlyUsedStrategy implements ContextAwareStrategy {

    private final LbInstanceRepository repo;
    private final Clock clock;

    public LeastRecentlyUsedStrategy(LbInstanceRepository repo, Clock clock) {
        this.repo = repo;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Optional<String> choose(String tenantId, String serviceGroup) {
        // DB-backed pick to work across nodes
        return repo.pickLeastRecentlyUsedForUpdate(tenantId, serviceGroup)
                .map(inst -> {
                    inst.setLastUsedAt(OffsetDateTime.now(clock)); // update inside same TX
                    return inst.getAddress();
                });
    }
}
