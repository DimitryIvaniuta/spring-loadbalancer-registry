package com.github.dimitryivaniuta.loadbalancer.api;

import java.util.List;
import java.util.Optional;

public interface LoadBalancer {
    long register(String tenantId, String serviceGroup, String address);

    void unregister(String tenantId, String serviceGroup, String address);

    List<String> listAddresses(String tenantId, String serviceGroup);

    Optional<String> nextAddress(String tenantId, String serviceGroup);
}