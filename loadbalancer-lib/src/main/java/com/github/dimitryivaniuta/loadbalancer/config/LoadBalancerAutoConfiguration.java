package com.github.dimitryivaniuta.loadbalancer.config;

import com.github.dimitryivaniuta.loadbalancer.api.ContextAwareStrategy;
import com.github.dimitryivaniuta.loadbalancer.api.LoadBalancingStrategy;
import com.github.dimitryivaniuta.loadbalancer.domain.LbCursor;
import com.github.dimitryivaniuta.loadbalancer.domain.LbDecision;
import com.github.dimitryivaniuta.loadbalancer.domain.LbInstance;
import com.github.dimitryivaniuta.loadbalancer.repo.LbCursorRepository;
import com.github.dimitryivaniuta.loadbalancer.repo.LbDecisionRepository;
import com.github.dimitryivaniuta.loadbalancer.repo.LbInstanceRepository;
import com.github.dimitryivaniuta.loadbalancer.service.DbCursorService;
import com.github.dimitryivaniuta.loadbalancer.service.DecisionContext;
import com.github.dimitryivaniuta.loadbalancer.service.LoadBalancerService;
import com.github.dimitryivaniuta.loadbalancer.strategy.*;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
@EntityScan(basePackageClasses = { LbInstance.class, LbDecision.class, LbCursor.class })
@EnableJpaRepositories(basePackageClasses = { LbInstanceRepository.class, LbDecisionRepository.class, LbCursorRepository.class })
@Import(LoadBalancerService.class) // must be created by Spring so @Lookup works
public class LoadBalancerAutoConfiguration {

    /**
     * Required for auditing decisions per request (@Lookup creates a new instance each call).
     * Conditional to avoid "multiple DecisionContext beans" if you later annotate it with @Component.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @ConditionalOnMissingBean(DecisionContext.class)
    public DecisionContext decisionContext() {
        return new DecisionContext();
    }

    @Bean
    @ConditionalOnMissingBean
    public Clock loadBalancerClock() {
        return Clock.systemUTC();
    }

    /**
     * DB cursor service used by DISTRIBUTED_ROUND_ROBIN.
     * Created lazily via ObjectProvider in the strategy bean, but we still expose the service if repo is on classpath.
     */
    @Bean
    @ConditionalOnClass(LbCursorRepository.class)
    @ConditionalOnMissingBean
    public DbCursorService dbCursorService(LbCursorRepository repo) {
        return new DbCursorService(repo);
    }

    /**
     * Exactly ONE LoadBalancingStrategy bean in the context.
     * - LRU is handled by ContextAwareStrategy (below), so we keep a safe fallback (RR) here.
     */
    @Bean
    @ConditionalOnMissingBean(LoadBalancingStrategy.class)
    public LoadBalancingStrategy loadBalancingStrategy(
            LoadBalancerProperties props,
            ObjectProvider<DbCursorService> cursorProvider
    ) {
        var strategy = props.getStrategy();

        if (strategy == null) return new RoundRobinStrategy();

        return switch (strategy) {
            case RANDOM -> new RandomStrategy();
            case WEIGHTED_RANDOM -> new WeightedRandomStrategy(props);
            case DISTRIBUTED_ROUND_ROBIN -> new DistributedRoundRobinStrategy(cursorProvider.getObject());
            case LEAST_RECENTLY_USED -> new RoundRobinStrategy(); // real LRU is ContextAwareStrategy below
            case ROUND_ROBIN -> new RoundRobinStrategy();
        };
    }

    /**
     * LRU must be tenant+serviceGroup aware and DB-backed (FOR UPDATE SKIP LOCKED).
     * LoadBalancerService should prefer this bean when present.
     */
    @Bean
    @ConditionalOnProperty(prefix = "loadbalancer", name = "strategy", havingValue = "LEAST_RECENTLY_USED")
    @ConditionalOnMissingBean(ContextAwareStrategy.class)
    public ContextAwareStrategy leastRecentlyUsedStrategy(LbInstanceRepository repo, Clock clock) {
        return new LeastRecentlyUsedStrategy(repo, clock);
    }
}
