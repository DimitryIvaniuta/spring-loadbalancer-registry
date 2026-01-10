package com.github.dimitryivaniuta.loadbalancer.repo;

import com.github.dimitryivaniuta.loadbalancer.domain.LbInstance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LbInstanceRepository extends JpaRepository<LbInstance, Long> {

    Optional<LbInstance> findByAddress(String address);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from LbInstance i order by i.id asc")
    List<LbInstance> findAllForUpdate();

    @Modifying
    @Query("delete from LbInstance i where i.address = :address")
    int deleteByAddress(@Param("address") String address);
}
