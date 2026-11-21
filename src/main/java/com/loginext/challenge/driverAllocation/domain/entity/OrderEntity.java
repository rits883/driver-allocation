
package com.loginext.challenge.driverAllocation.domain.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_reference", nullable = false,
            unique = true, length = 100)
    private String orderReference;

    @Column(name = "order_time", nullable = false)
    private Long orderTime;

    @Column(name = "travel_time", nullable = false)
    private Long travelTime;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false,
            insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected OrderEntity() {
        // Required by JPA
    }

    public OrderEntity(
            String orderReference,
            long orderTime,
            long travelTime) {

        if (orderReference == null || orderReference.isBlank()) {
            throw new IllegalArgumentException(
                    "Order reference is required");
        }

        if (orderTime < 0 || travelTime <= 0) {
            throw new IllegalArgumentException(
                    "Invalid order or travel time");
        }

        this.orderReference = orderReference;
        this.orderTime = orderTime;
        this.travelTime = travelTime;
    }

    public Long getId() {
        return id;
    }

    public String getOrderReference() {
        return orderReference;
    }

    public Long getOrderTime() {
        return orderTime;
    }

    public Long getTravelTime() {
        return travelTime;
    }

    public String getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void markAssigned() {
        if (!"PENDING".equals(status)) {
            throw new IllegalStateException(
                    "Only pending orders can be assigned");
        }
        this.status = "ASSIGNED";
    }

    public void markRejected() {
        if (!"PENDING".equals(status)) {
            throw new IllegalStateException(
                    "Only pending orders can be rejected");
        }
        this.status = "REJECTED";
    }
}
