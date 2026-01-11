package com.github.dimitryivaniuta.loadbalancer.service;

import com.github.dimitryivaniuta.loadbalancer.api.ContextAwareStrategy;
import com.github.dimitryivaniuta.loadbalancer.api.LoadBalancer;
import com.github.dimitryivaniuta.loadbalancer.api.LoadBalancingStrategy;
import com.github.dimitryivaniuta.loadbalancer.config.LoadBalancerProperties;
import com.github.dimitryivaniuta.loadbalancer.domain.LbInstance;
import com.github.dimitryivaniuta.loadbalancer.exceptions.*;
import com.github.dimitryivaniuta.loadbalancer.repo.LbDecisionRepository;
import com.github.dimitryivaniuta.loadbalancer.repo.LbInstanceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Lookup;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class LoadBalancerService implements LoadBalancer {

    private final LbInstanceRepository repo;
    private final LoadBalancingStrategy strategy;
    private final LoadBalancerProperties props;
    private final LbDecisionRepository decisionRepo;

    @Lookup
    protected DecisionContext decisionContext() {
        return null; // Spring overrides at runtime
    }

    @Override
    @Transactional
    public long register(String tenantId, String serviceGroup, String address) {
        String t = normalizeKey(tenantId);
        String g = normalizeKey(serviceGroup);
        String a = normalize(address);

        List<LbInstance> locked = repo.findAllForUpdate(t, g);

        boolean exists = locked.stream().anyMatch(i -> i.getAddress().equals(a));
        if (exists) throw new DuplicateAddressException(a);

        if (locked.size() >= props.getMaxInstances()) throw new CapacityExceededException(props.getMaxInstances());

        LbInstance saved = repo.save(LbInstance.builder()
                .tenantId(t)
                .serviceGroup(g)
                .address(a)
                .createdAt(OffsetDateTime.now())
                .build());

        return saved.getId();
    }

    @Override
    @Transactional
    public void unregister(String address, String tenantId, String serviceGroup) {
        String normalizedAddress = normalize(address);
        int deleted = repo.deleteByTenantIdAndServiceGroupAndAddress(tenantId, serviceGroup, normalizedAddress);
        if (deleted == 0) {
            throw new InstanceNotFoundException(normalizedAddress);
        }
    }

    @Override
    @Transactional
    public List<String> listAddresses(String tenantId, String serviceGroup) {
        return repo.findAllByTenantIdAndServiceGroup(tenantId, serviceGroup).stream()
                .sorted(Comparator.comparing(LbInstance::getId))
                .map(LbInstance::getAddress)
                .toList();
    }

    @Override
    @Transactional
    public Optional<String> nextAddress(String tenantId, String serviceGroup) {
        List<String> addresses = listAddresses(tenantId, serviceGroup);

        DecisionContext ctx = decisionContext(); // <- NEW per call
        ctx.started(strategy.getClass().getSimpleName(), addresses.size());

        Optional<String> chosen = chooseStrategy(addresses, tenantId, serviceGroup);
        chosen.ifPresent(ctx::chosen);

        decisionRepo.save(ctx.toEntity());
        return chosen;
    }

    private Optional<String> chooseStrategy(List<String> addresses, String tenantId, String serviceGroup) {
        if (strategy instanceof ContextAwareStrategy ctx) {
            return ctx.choose(tenantId, serviceGroup);
        }
        return strategy.choose(addresses);
    }

    private static String normalizeKey(String v) {
        if (v == null) return "default";
        String x = v.trim();
        return x.isEmpty() ? "default" : x;
    }

    private static String normalize(String address) {
        if (address == null) throw new IllegalArgumentException("address must not be null");
        String v = address.trim();
        if (v.isEmpty()) throw new IllegalArgumentException("address must not be blank");
        return v;
    }
}
