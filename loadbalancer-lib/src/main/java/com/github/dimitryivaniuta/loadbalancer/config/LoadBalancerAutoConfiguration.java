package com.github.dimitryivaniuta.loadbalancer.config;

import com.github.dimitryivaniuta.loadbalancer.api.LoadBalancingStrategy;
import com.github.dimitryivaniuta.loadbalancer.api.RoundRobinStrategy;
import com.github.dimitryivaniuta.loadbalancer.domain.LbInstance;
import com.github.dimitryivaniuta.loadbalancer.repo.LbInstanceRepository;
import com.github.dimitryivaniuta.loadbalancer.service.LoadBalancerService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EnableConfigurationProperties(LoadBalancerProperties.class)
@EntityScan(basePackageClasses = LbInstance.class)
@EnableJpaRepositories(basePackageClasses = LbInstanceRepository.class)
public class LoadBalancerAutoConfiguration {

    @Bean
    public LoadBalancingStrategy loadBalancingStrategy() {
        return new RoundRobinStrategy();
    }

    @Bean
    public LoadBalancerService loadBalancerService(
            LbInstanceRepository repo,
            LoadBalancingStrategy strategy,
            LoadBalancerProperties props
    ) {
        return new LoadBalancerService(repo, strategy, props);
    }
}
