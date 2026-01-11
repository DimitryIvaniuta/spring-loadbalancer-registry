package com.github.dimitryivaniuta.loadbalancer.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "lb_instance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LbInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address", nullable = false, length = 512, unique = true)
    private String address;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "service_group", nullable = false, length = 64)
    private String serviceGroup;
}
