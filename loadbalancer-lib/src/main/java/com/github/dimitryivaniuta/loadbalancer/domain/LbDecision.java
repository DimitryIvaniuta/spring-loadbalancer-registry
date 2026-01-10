package com.github.dimitryivaniuta.loadbalancer.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "lb_decision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LbDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;

    @Column(name = "strategy", nullable = false, length = 64)
    private String strategy;

    @Column(name = "candidates", nullable = false)
    private int candidates;

    @Column(name = "chosen_address", length = 512)
    private String chosenAddress;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
