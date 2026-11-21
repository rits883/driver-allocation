package com.loginext.challenge.driverAllocation.repository;

import com.loginext.challenge.driverAllocation.domain.entity.DriverEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<DriverEntity, Long> {

    Optional<DriverEntity> findByDriverCode(String driverCode);

    @Query("""
           SELECT d
           FROM DriverEntity d
           WHERE d.status = 'ACTIVE'
             AND d.availableAt <= :orderTime
           ORDER BY d.id ASC
           """)
    List<DriverEntity> findAvailableDrivers(
            @Param("orderTime") long orderTime,
            Pageable pageable
    );

    boolean existsByDriverCode(String driverCode);
}