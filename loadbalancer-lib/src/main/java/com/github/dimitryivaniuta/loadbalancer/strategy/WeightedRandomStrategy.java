package com.github.dimitryivaniuta.loadbalancer.strategy;

import com.github.dimitryivaniuta.loadbalancer.api.LoadBalancingStrategy;
import com.github.dimitryivaniuta.loadbalancer.config.LoadBalancerProperties;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

public final class WeightedRandomStrategy implements LoadBalancingStrategy {

    private final SecureRandom rnd = new SecureRandom();
    private final LoadBalancerProperties props;

    public WeightedRandomStrategy(LoadBalancerProperties props) {
        this.props = props;
    }

    @Override
    public Optional<String> choose(List<String> addresses) {
        if (addresses == null || addresses.isEmpty()) return Optional.empty();

        int total = 0;
        int[] w = new int[addresses.size()];

        for (int i = 0; i < addresses.size(); i++) {
            int weight = props.getWeights().getOrDefault(addresses.get(i), 1);
            if (weight < 0) weight = 0;
            w[i] = weight;
            total += weight;
        }

        // if all weights are 0 -> fallback to uniform random
        if (total <= 0) {
            return Optional.of(addresses.get(rnd.nextInt(addresses.size())));
        }

        int pick = rnd.nextInt(total);
        int acc = 0;
        for (int i = 0; i < w.length; i++) {
            acc += w[i];
            if (pick < acc) return Optional.of(addresses.get(i));
        }
        return Optional.of(addresses.get(addresses.size() - 1));
    }
}
