package com.github.dimitryivaniuta.loadbalancer.api;

import java.util.List;
import java.util.Optional;

public interface LoadBalancingStrategy {
    Optional<String> choose(List<String> addresses);
}
