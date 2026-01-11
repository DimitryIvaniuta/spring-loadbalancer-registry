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

    /**
     * JPA PESSIMISTIC_WRITE alone tends to make concurrent calls serialize on the same row.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from LbInstance i order by i.id asc")
    List<LbInstance> findAllForUpdate();

    @Modifying
    @Query("delete from LbInstance i where i.address = :address")
    int deleteByAddress(@Param("address") String address);

    /**
     * SKIP LOCKED is what makes it truly distribute under load.
     */
    @Query(value = """
            select *
            from lb_instance
            order by last_used_at asc nulls first, id asc
            for update skip locked
            limit 1
            """, nativeQuery = true)
    Optional<LbInstance> pickLeastRecentlyUsedForUpdateAll();

    Optional<LbInstance> findByTenantIdAndServiceGroupAndAddress(String tenantId, String serviceGroup, String address);

    @Modifying
    @Query("""
              delete from LbInstance i
              where i.tenantId = :tenantId and i.serviceGroup = :serviceGroup and i.address = :address
            """)
    int deleteByTenantIdAndServiceGroupAndAddress(
            @Param("tenantId") String tenantId,
            @Param("serviceGroup") String serviceGroup,
            @Param("address") String address
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
              select i from LbInstance i
              where i.tenantId = :tenantId and i.serviceGroup = :serviceGroup
              order by i.id asc
            """)
    List<LbInstance> findAllForUpdate(
            @Param("tenantId") String tenantId,
            @Param("serviceGroup") String serviceGroup
    );

    @Query(value = """
              select *
              from lb_instance
              where tenant_id = :tenantId
                and service_group = :serviceGroup
              order by last_used_at asc nulls first, id asc
              for update skip locked
              limit 1
            """, nativeQuery = true)
    Optional<LbInstance> pickLeastRecentlyUsedForUpdate(
            @Param("tenantId") String tenantId,
            @Param("serviceGroup") String serviceGroup
    );

    @Query("""
              select i from LbInstance i
              where i.tenantId = :tenantId and i.serviceGroup = :serviceGroup
              order by i.id asc
            """)
    List<LbInstance> findAllByTenantIdAndServiceGroup(
            @Param("tenantId") String tenantId,
            @Param("serviceGroup") String serviceGroup
    );

}
