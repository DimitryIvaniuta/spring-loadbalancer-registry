package com.github.dimitryivaniuta.loadbalancer.service;

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
    public long register(String address) {
        String normalized = normalize(address);

        // Lock rows to enforce capacity safely under concurrent registrations.
        List<LbInstance> locked = repo.findAllForUpdate();

        boolean exists = locked.stream().anyMatch(i -> i.getAddress().equals(normalized));
        if (exists) throw new DuplicateAddressException(normalized);

        if (locked.size() >= props.getMaxInstances()) throw new CapacityExceededException(props.getMaxInstances());

        LbInstance saved = repo.save(LbInstance.builder()
                .address(normalized)
                .createdAt(OffsetDateTime.now())
                .build());

        return saved.getId();
    }

    @Override
    @Transactional
    public void unregister(String address) {
        String normalized = normalize(address);
        int deleted = repo.deleteByAddress(normalized);
        if (deleted == 0) throw new InstanceNotFoundException(normalized);
    }

    @Override
    @Transactional
    public List<String> listAddresses() {
        return repo.findAll().stream()
                .sorted(Comparator.comparing(LbInstance::getId))
                .map(LbInstance::getAddress)
                .toList();
    }

    @Override
    @Transactional
    public Optional<String> nextAddress() {
        List<String> addresses = listAddresses();

        DecisionContext ctx = decisionContext(); // <- NEW per call
        ctx.started(strategy.getClass().getSimpleName(), addresses.size());

        Optional<String> chosen = strategy.choose(addresses);
        chosen.ifPresent(ctx::chosen);

        decisionRepo.save(ctx.toEntity());
        return chosen;
    }


    private static String normalize(String address) {
        if (address == null) throw new IllegalArgumentException("address must not be null");
        String v = address.trim();
        if (v.isEmpty()) throw new IllegalArgumentException("address must not be blank");
        return v;
    }
}
