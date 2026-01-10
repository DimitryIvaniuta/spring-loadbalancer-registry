package com.github.dimitryivaniuta.loadbalancer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "loadbalancer")
public class LoadBalancerProperties {
    /**
     * Max number of registered addresses allowed.
     */
    private int maxInstances = 10;
}
