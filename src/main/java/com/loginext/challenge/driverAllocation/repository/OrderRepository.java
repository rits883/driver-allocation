package com.loginext.challenge.driverAllocation.repository;

import com.loginext.challenge.driverAllocation.domain.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByOrderReference(String orderReference);
}