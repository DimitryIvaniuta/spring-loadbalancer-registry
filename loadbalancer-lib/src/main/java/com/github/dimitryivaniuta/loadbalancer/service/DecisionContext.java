package com.github.dimitryivaniuta.loadbalancer.service;

import com.github.dimitryivaniuta.loadbalancer.domain.LbDecision;
import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE) // <- NEW INSTANCE per lookup
public class DecisionContext {

    @Getter
    private final String requestId = UUID.randomUUID().toString();

    private String strategy;
    private int candidates;
    private String chosen;

    public void started(String strategy, int candidates) {
        this.strategy = strategy;
        this.candidates = candidates;
    }

    public void chosen(String address) {
        this.chosen = address;
    }

    public LbDecision toEntity() {
        return LbDecision.builder()
                .requestId(requestId)
                .strategy(strategy)
                .candidates(candidates)
                .chosenAddress(chosen)
                .createdAt(OffsetDateTime.now())
                .build();
    }
}
