package com.github.dimitryivaniuta.loadbalancer.strategy;

import com.github.dimitryivaniuta.loadbalancer.api.LoadBalancingStrategy;
import com.github.dimitryivaniuta.loadbalancer.service.DbCursorService;

import java.util.List;
import java.util.Optional;

public final class DistributedRoundRobinStrategy implements LoadBalancingStrategy {

    private final DbCursorService cursor;

    public DistributedRoundRobinStrategy(DbCursorService cursor) {
        this.cursor = cursor;
    }

    @Override
    public Optional<String> choose(List<String> addresses) {
        if (addresses == null || addresses.isEmpty()) return Optional.empty();
        int idx = cursor.nextSlot(addresses.size());
        return Optional.of(addresses.get(idx));
    }
}
