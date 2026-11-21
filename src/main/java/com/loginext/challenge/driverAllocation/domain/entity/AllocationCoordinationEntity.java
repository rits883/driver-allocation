
package com.loginext.challenge.driverAllocation.domain.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "allocation_coordination")
public class AllocationCoordinationEntity {

    @Id
    private Short id;

    @Column(name = "last_order_time", nullable = false)
    private Long lastOrderTime;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected AllocationCoordinationEntity() {
        // Required by JPA
    }

    public Short getId() {
        return id;
    }

    public Long getLastOrderTime() {
        return lastOrderTime;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void updateLastOrderTime(long orderTime) {
        if (orderTime < lastOrderTime) {
            throw new IllegalArgumentException(
                    "Order time cannot move backwards"
            );
        }

        this.lastOrderTime = orderTime;
        this.updatedAt = OffsetDateTime.now();
    }
}
