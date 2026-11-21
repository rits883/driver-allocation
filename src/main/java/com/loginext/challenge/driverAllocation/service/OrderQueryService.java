package com.loginext.challenge.driverAllocation.service;

import com.loginext.challenge.driverAllocation.domain.entity.OrderEntity;
import com.loginext.challenge.driverAllocation.domain.entity.DriverAllocationEntity;

import com.loginext.challenge.driverAllocation.dto.OrderResponse;

import com.loginext.challenge.driverAllocation.repository.DriverAllocationRepository;
import com.loginext.challenge.driverAllocation.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final DriverAllocationRepository allocationRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("id", "orderReference", "orderTime", "travelTime", "status");

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page must be >= 0 and size must be between 1 and 100"
            );
        }

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sort field: " + sortBy
            );
        }

        Sort.Direction sortDirection;

        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Direction must be asc or desc"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
                        .and(Sort.by("id"))
        );

        Page<OrderEntity> orders = orderRepository.findAll(pageable);

        if (orders.isEmpty()) {
            return orders.map(order -> toResponse(order, null));
        }

        var orderIds = orders.getContent()
                .stream()
                .map(OrderEntity::getId)
                .toList();

        Map<Long, DriverAllocationEntity> allocations =
                allocationRepository.findByOrderIds(orderIds)
                        .stream()
                        .collect(Collectors.toMap(
                                allocation -> allocation.getOrder().getId(),
                                Function.identity(),
                                (first, second) -> first
                        ));

        return orders.map(order ->
                toResponse(order, allocations.get(order.getId()))
        );
    }

    private OrderResponse toResponse(
            OrderEntity order,
            DriverAllocationEntity allocation
    ) {
        Long driverId = allocation == null
                ? null
                : allocation.getDriver().getId();

        return new OrderResponse(
                order.getId(),
                order.getOrderReference(),
                order.getOrderTime(),
                order.getTravelTime(),
                order.getStatus(),
                driverId
        );
    }
}