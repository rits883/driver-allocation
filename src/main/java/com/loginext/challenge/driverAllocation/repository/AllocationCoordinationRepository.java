
package com.loginext.challenge.driverAllocation.repository;

import com.loginext.challenge.driverAllocation.domain.entity.AllocationCoordinationEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AllocationCoordinationRepository
        extends JpaRepository<AllocationCoordinationEntity, Short> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c
            FROM AllocationCoordinationEntity c
            WHERE c.id = :id
            """)
    Optional<AllocationCoordinationEntity> findByIdForUpdate(
            @Param("id") Short id
    );






}
