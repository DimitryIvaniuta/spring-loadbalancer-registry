package com.github.dimitryivaniuta.loadbalancer.api;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class RoundRobinStrategy implements LoadBalancingStrategy {

    private final AtomicLong cursor = new AtomicLong(0);

    @Override
    public Optional<String> choose(List<String> addresses) {
        if (addresses == null || addresses.isEmpty()) return Optional.empty();
        long idx = Math.floorMod(cursor.getAndIncrement(), addresses.size());
        return Optional.of(addresses.get((int) idx));
    }
}
