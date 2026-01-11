package com.github.dimitryivaniuta.loadbalancer.api;

import java.util.Optional;

public interface ContextAwareStrategy {
    Optional<String> choose(String tenantId, String serviceGroup);
}