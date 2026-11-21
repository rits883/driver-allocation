
package com.loginext.challenge.driverAllocation.repository;

import com.loginext.challenge.driverAllocation.domain.entity.AllocationCoordinationEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AllocationCoordinationRepositoryTest {

    @Autowired
    private AllocationCoordinationRepository repository;

    @Test
    @Transactional
    void shouldAcquireCoordinationRow() {

        AllocationCoordinationEntity coordination =
                repository.findByIdForUpdate((short) 1)
                        .orElseThrow();

        assertEquals((short) 1, coordination.getId());
        assertTrue(coordination.getLastOrderTime() >= -1);
        assertNotNull(coordination.getUpdatedAt());
    }

    @Test
    @Transactional
    void shouldUpdateLastOrderTimeWithinTransaction() {

        AllocationCoordinationEntity coordination =
                repository.findByIdForUpdate((short) 1)
                        .orElseThrow();

        long previousTime = coordination.getLastOrderTime();

        coordination.updateLastOrderTime(previousTime + 10);

        repository.flush();

        assertEquals(
                previousTime + 10,
                coordination.getLastOrderTime()
        );
    }
}
