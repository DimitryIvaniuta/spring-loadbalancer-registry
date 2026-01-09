package com.github.dimitryivaniuta.loadbalancer.api;

import java.util.List;
import java.util.Optional;

public interface LoadBalancer {
    long register(String address);

    void unregister(String address);

    List<String> listAddresses();

    Optional<String> nextAddress();
}