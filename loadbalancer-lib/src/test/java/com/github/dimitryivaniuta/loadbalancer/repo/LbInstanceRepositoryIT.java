package com.github.dimitryivaniuta.loadbalancer.repo;

import com.github.dimitryivaniuta.loadbalancer.domain.LbInstance;
import com.github.dimitryivaniuta.loadbalancer.infra.AbstractPostgresIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LbInstanceRepositoryIT extends AbstractPostgresIT {

    @Autowired
    LbInstanceRepository repo;

    @Test
    void uniqueAddressIsEnforcedByDb() {
        repo.save(LbInstance.builder()
                .address("http://a")
                .createdAt(OffsetDateTime.now())
                .build());

        assertThatThrownBy(() ->
                repo.saveAndFlush(LbInstance.builder()
                        .address("http://a")
                        .createdAt(OffsetDateTime.now())
                        .build())
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
