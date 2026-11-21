
package com.loginext.challenge.driverAllocation.repository;

import com.loginext.challenge.driverAllocation.domain.entity.DriverAllocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DriverAllocationRepository
        extends JpaRepository<DriverAllocationEntity, Long> {

    Optional<DriverAllocationEntity> findByOrderId(Long orderId);

    List<DriverAllocationEntity> findByDriverId(Long driverId);

        @Query("""
            SELECT a
            FROM DriverAllocationEntity a
            JOIN FETCH a.driver
            WHERE a.order.id IN :orderIds
            """)
        List<DriverAllocationEntity> findByOrderIds(
                @Param("orderIds") Collection<Long> orderIds
        );


}
