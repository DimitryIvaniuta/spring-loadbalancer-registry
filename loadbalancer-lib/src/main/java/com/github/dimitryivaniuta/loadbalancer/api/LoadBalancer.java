package com.github.dimitryivaniuta.loadbalancer.api;

import java.util.List;
import java.util.Optional;

public interface LoadBalancer {
    long register(RegistryScope scope, String address);

    void unregister(RegistryScope scope, String address);

    List<String> listAddresses(RegistryScope scope);

    Optional<String> nextAddress(RegistryScope scope);
}