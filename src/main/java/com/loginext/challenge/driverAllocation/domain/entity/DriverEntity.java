
package com.loginext.challenge.driverAllocation.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "drivers")
public class DriverEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "driver_code",
            nullable = false,
            unique = true,
            length = 50,
            updatable = false
    )
    private String driverCode;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "available_at", nullable = false)
    private Long availableAt = 0L;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    public DriverEntity(String driverCode) {
        if (driverCode == null || driverCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Driver code cannot be blank"
            );
        }

        String normalizedCode = driverCode.trim();

        if (normalizedCode.length() > 50) {
            throw new IllegalArgumentException(
                    "Driver code cannot exceed 50 characters"
            );
        }

        this.driverCode = normalizedCode;
    }

    public void updateAvailability(long availableAt) {
        if (availableAt < 0) {
            throw new IllegalArgumentException(
                    "Availability cannot be negative"
            );
        }

        this.availableAt = availableAt;
    }

    public void activate() {
        this.status = "ACTIVE";
    }

    public void deactivate() {
        this.status = "INACTIVE";
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}
