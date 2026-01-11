package com.github.dimitryivaniuta.loadbalancer.repo;

import com.github.dimitryivaniuta.loadbalancer.domain.LbInstance;
import com.github.dimitryivaniuta.loadbalancer.infra.AbstractPostgresIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class LbInstanceRepositoryIT extends AbstractPostgresIT {

    private static final String TENANT = "tenant-1";
    private static final String GROUP = "payments";

    @Autowired
    LbInstanceRepository repo;

    @BeforeEach
    void clean() {
        repo.deleteAll();
    }

    @Test
    void uniqueAddressIsEnforcedPerTenantAndGroup_byDbConstraint() {
        repo.saveAndFlush(instance(TENANT, GROUP, "http://a"));

        assertThatThrownBy(() -> repo.saveAndFlush(instance(TENANT, GROUP, "http://a")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void sameAddressAllowedAcrossDifferentTenantOrGroup() {
        repo.saveAndFlush(instance(TENANT, GROUP, "http://a"));

        // different group -> OK
        assertThatCode(() -> repo.saveAndFlush(instance(TENANT, "shipping", "http://a")))
                .doesNotThrowAnyException();

        // different tenant -> OK
        assertThatCode(() -> repo.saveAndFlush(instance("tenant-2", GROUP, "http://a")))
                .doesNotThrowAnyException();
    }

    private static LbInstance instance(String tenantId, String serviceGroup, String address) {
        return LbInstance.builder()
                .tenantId(tenantId)
                .serviceGroup(serviceGroup)
                .address(address)
                .createdAt(OffsetDateTime.now())
                .build();
    }
}
