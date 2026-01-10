package com.github.dimitryivaniuta.loadbalancer.config;

import com.github.dimitryivaniuta.loadbalancer.api.LoadBalancingStrategy;
import com.github.dimitryivaniuta.loadbalancer.api.RoundRobinStrategy;
import com.github.dimitryivaniuta.loadbalancer.domain.LbDecision;
import com.github.dimitryivaniuta.loadbalancer.domain.LbInstance;
import com.github.dimitryivaniuta.loadbalancer.repo.LbDecisionRepository;
import com.github.dimitryivaniuta.loadbalancer.repo.LbInstanceRepository;
import com.github.dimitryivaniuta.loadbalancer.service.DecisionContext;
import com.github.dimitryivaniuta.loadbalancer.service.LoadBalancerService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EnableConfigurationProperties(LoadBalancerProperties.class)
@EntityScan(basePackageClasses = { LbInstance.class, LbDecision.class })
@EnableJpaRepositories(basePackageClasses = { LbInstanceRepository.class, LbDecisionRepository.class })
@Import(LoadBalancerService.class) // container creates it => @Lookup works
public class LoadBalancerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LoadBalancingStrategy loadBalancingStrategy() {
        return new RoundRobinStrategy();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DecisionContext decisionContext() {
        return new DecisionContext(); // or inject deps if it has any
    }

/*    @Bean
    public LoadBalancingStrategy loadBalancingStrategy() {
        return new RoundRobinStrategy();
    }

    @Bean
    public LoadBalancerService loadBalancerService(
            LbInstanceRepository repo,
            LoadBalancingStrategy strategy,
            LoadBalancerProperties props,
            LbDecisionRepository decisionRepo
    ) {
        return new LoadBalancerService(repo, strategy, props, decisionRepo);
    }*/
}
