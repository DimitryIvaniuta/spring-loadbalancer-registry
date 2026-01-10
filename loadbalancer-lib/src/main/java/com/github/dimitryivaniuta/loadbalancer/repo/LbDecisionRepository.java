package com.github.dimitryivaniuta.loadbalancer.repo;

import com.github.dimitryivaniuta.loadbalancer.domain.LbDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LbDecisionRepository extends JpaRepository<LbDecision, Long> {
    Optional<LbDecision> findFirstByOrderByCreatedAtDesc();
}
