package com.github.dimitryivaniuta.loadbalancer.strategy;

import com.github.dimitryivaniuta.loadbalancer.api.LoadBalancingStrategy;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

public final class RandomStrategy implements LoadBalancingStrategy {

    private final SecureRandom rnd = new SecureRandom();

    @Override
    public Optional<String> choose(List<String> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(addresses.get(rnd.nextInt(addresses.size())));
    }
}
