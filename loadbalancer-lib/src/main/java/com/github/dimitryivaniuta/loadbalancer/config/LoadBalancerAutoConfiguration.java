package com.github.dimitryivaniuta.loadbalancer.config;

import com.github.dimitryivaniuta.loadbalancer.api.LoadBalancingStrategy;
import com.github.dimitryivaniuta.loadbalancer.repo.LbCursorRepository;
import com.github.dimitryivaniuta.loadbalancer.service.DbCursorService;
import com.github.dimitryivaniuta.loadbalancer.strategy.*;
import com.github.dimitryivaniuta.loadbalancer.domain.LbDecision;
import com.github.dimitryivaniuta.loadbalancer.domain.LbInstance;
import com.github.dimitryivaniuta.loadbalancer.repo.LbDecisionRepository;
import com.github.dimitryivaniuta.loadbalancer.repo.LbInstanceRepository;
import com.github.dimitryivaniuta.loadbalancer.service.DecisionContext;
import com.github.dimitryivaniuta.loadbalancer.service.LoadBalancerService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.Clock;

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

    @Bean
    @ConditionalOnProperty(prefix = "loadbalancer", name = "strategy", havingValue = "RANDOM")
    public LoadBalancingStrategy randomStrategy() {
        return new RandomStrategy();
    }

    @Bean
    @ConditionalOnProperty(prefix = "loadbalancer", name = "strategy", havingValue = "WEIGHTED_RANDOM")
    public LoadBalancingStrategy weightedRandomStrategy(LoadBalancerProperties props) {
        return new WeightedRandomStrategy(props);
    }

    @Bean
    @ConditionalOnProperty(prefix = "loadbalancer", name = "strategy", havingValue = "DISTRIBUTED_ROUND_ROBIN")
    public LoadBalancingStrategy distributedRoundRobinStrategy(DbCursorService cursor) {
        return new DistributedRoundRobinStrategy(cursor);
    }

    @Bean
    @ConditionalOnMissingBean(LoadBalancingStrategy.class)
    public LoadBalancingStrategy defaultRoundRobinStrategy() {
        return new RoundRobinStrategy();
    }

    @Bean
    @ConditionalOnMissingBean
    public DbCursorService dbCursorService(LbCursorRepository repo) {
        return new DbCursorService(repo);
    }

    @Bean
    @ConditionalOnMissingBean
    public Clock loadBalancerClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnProperty(prefix = "loadbalancer", name = "strategy", havingValue = "LEAST_RECENTLY_USED")
    public LoadBalancingStrategy leastRecentlyUsedStrategy(LbInstanceRepository repo, Clock clock) {
        return new LeastRecentlyUsedStrategy(repo, clock);
    }
}
