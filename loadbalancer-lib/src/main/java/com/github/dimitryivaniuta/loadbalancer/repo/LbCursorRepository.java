package com.github.dimitryivaniuta.loadbalancer.repo;

import com.github.dimitryivaniuta.loadbalancer.domain.LbCursor;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LbCursorRepository extends JpaRepository<LbCursor, Short> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from LbCursor c where c.id = 1")
    Optional<LbCursor> lockSingleton();
}
