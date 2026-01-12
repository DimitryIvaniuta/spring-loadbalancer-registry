package com.github.dimitryivaniuta.loadbalancer.strategy;

import com.github.dimitryivaniuta.loadbalancer.infra.AbstractPostgresIT;
import com.github.dimitryivaniuta.loadbalancer.repo.LbCursorRepository;
import com.github.dimitryivaniuta.loadbalancer.service.DbCursorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DistributedRoundRobinStrategyIT extends AbstractPostgresIT {

    @Autowired
    LbCursorRepository cursorRepo;

    @Test
    void distributedRoundRobinUsesDbCursor() {
        DbCursorService cursor = new DbCursorService(cursorRepo);
        var s1 = new DistributedRoundRobinStrategy(cursor);
        var s2 = new DistributedRoundRobinStrategy(cursor);

        List<String> a = List.of("A", "B", "C");

        // alternating calls simulate multiple nodes:
        assertThat(s1.choose(a)).contains("A");
        assertThat(s2.choose(a)).contains("B");
        assertThat(s1.choose(a)).contains("C");
        assertThat(s2.choose(a)).contains("A");
    }
}
