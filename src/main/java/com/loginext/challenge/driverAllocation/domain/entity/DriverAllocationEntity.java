
package com.loginext.challenge.driverAllocation.domain.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "driver_allocations")
public class DriverAllocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false,
            unique = true)
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private DriverEntity driver;

    @Column(name = "allocated_at", nullable = false,
            insertable = false, updatable = false)
    private OffsetDateTime allocatedAt;

    protected DriverAllocationEntity() {
        // Required by JPA
    }

    public DriverAllocationEntity(
            OrderEntity order,
            DriverEntity driver) {

        this.order = java.util.Objects.requireNonNull(order);
        this.driver = java.util.Objects.requireNonNull(driver);
    }

    public Long getId() {
        return id;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public DriverEntity getDriver() {
        return driver;
    }

    public OffsetDateTime getAllocatedAt() {
        return allocatedAt;
    }
}
