package com.github.dimitryivaniuta.loadbalancer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "loadbalancer")
public class LoadBalancerProperties {
    /**
     * Max number of registered addresses allowed.
     */
    private int maxInstances = 10;

    private Strategy strategy = Strategy.ROUND_ROBIN;

    /**
     * Used by WEIGHTED_RANDOM. Default weight is 1 if not present.
     * Example:
     * loadbalancer.weights.http://10.0.0.1:8080=5
     * loadbalancer.weights.http://10.0.0.2:8080=1
     */
    private Map<String, Integer> weights = new HashMap<>();

    public enum Strategy {
        ROUND_ROBIN,
        DISTRIBUTED_ROUND_ROBIN,
        RANDOM,
        WEIGHTED_RANDOM,
        LEAST_RECENTLY_USED
    }
}
