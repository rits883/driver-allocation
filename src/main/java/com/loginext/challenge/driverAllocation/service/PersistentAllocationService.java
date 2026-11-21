
package com.loginext.challenge.driverAllocation.service;

import com.loginext.challenge.driverAllocation.domain.entity.*;
import com.loginext.challenge.driverAllocation.dto.*;
import com.loginext.challenge.driverAllocation.exception.DuplicateOrderConflictException;
import com.loginext.challenge.driverAllocation.exception.InvalidOrderSequenceException;
import com.loginext.challenge.driverAllocation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PersistentAllocationService {

    private final AllocationCoordinationRepository coordinationRepository;
    private final DriverRepository driverRepository;
    private final OrderRepository orderRepository;
    private final DriverAllocationRepository allocationRepository;

    @Transactional
    public OrderAllocationResponse allocate(CreateOrderRequest request) {


        AllocationCoordinationEntity coordination =
                coordinationRepository.findByIdForUpdate((short) 1)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Allocation coordination row missing"));


        var existingOrder =
                orderRepository.findByOrderReference(
                        request.orderReference());

        if (existingOrder.isPresent()) {
            return existingResponse(existingOrder.get(), request);
        }


        if (request.orderTime() < coordination.getLastOrderTime()) {
            throw new InvalidOrderSequenceException(
                    "Order time cannot be earlier than last processed order");
        }


        var drivers = driverRepository.findAvailableDrivers(
                request.orderTime(),
                PageRequest.of(0, 1)
        );

        OrderEntity order = new OrderEntity(
                request.orderReference(),
                request.orderTime(),
                request.travelTime()
        );

        if (drivers.isEmpty()) {

            order.markRejected();
            orderRepository.save(order);

            coordination.updateLastOrderTime(request.orderTime());

            return new OrderAllocationResponse(
                    order.getOrderReference(),
                    "REJECTED",
                    null,
                    null
            );
        }

        DriverEntity driver = drivers.get(0);

        // 5. Calculate availability with overflow protection.
        long availableAt = Math.addExact(
                request.orderTime(),
                request.travelTime()
        );

        driver.updateAvailability(availableAt);
        order.markAssigned();

        orderRepository.save(order);

        DriverAllocationEntity allocation =
                new DriverAllocationEntity(order, driver);

        allocationRepository.save(allocation);

        coordination.updateLastOrderTime(request.orderTime());

        // 6. All database changes commit together.
        return new OrderAllocationResponse(
                order.getOrderReference(),
                "ASSIGNED",
                driver.getId(),
                availableAt
        );
    }

    private OrderAllocationResponse existingResponse(
            OrderEntity order,
            CreateOrderRequest request) {

        if (order.getOrderTime() != request.orderTime()
                || order.getTravelTime() != request.travelTime()) {
            throw new DuplicateOrderConflictException(
                    "Order reference already exists with different details"
            );
        }


        if ("REJECTED".equals(order.getStatus())) {
            return new OrderAllocationResponse(
                    order.getOrderReference(),
                    "REJECTED",
                    null,
                    null
            );
        }

        DriverAllocationEntity allocation =
                allocationRepository.findByOrderId(order.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Assigned order has no allocation"));

        return new OrderAllocationResponse(
                order.getOrderReference(),
                "ASSIGNED",
                allocation.getDriver().getId(),
                Math.addExact(order.getOrderTime(), order.getTravelTime())
        );
    }
}
