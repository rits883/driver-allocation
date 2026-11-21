
package com.loginext.challenge.driverAllocation.service;

import com.loginext.challenge.driverAllocation.domain.entity.DriverEntity;
import com.loginext.challenge.driverAllocation.dto.CreateDriverRequest;
import com.loginext.challenge.driverAllocation.dto.DriverResponse;
import com.loginext.challenge.driverAllocation.dto.DriverStatusRequest;
import com.loginext.challenge.driverAllocation.exception.BusinessException;
import com.loginext.challenge.driverAllocation.repository.AllocationCoordinationRepository;
import com.loginext.challenge.driverAllocation.repository.DriverRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class DriverService {

    private final DriverRepository repository;
    private final AllocationCoordinationRepository allocationCoordinationRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("id", "driverCode", "status", "availableAt", "createdAt");



    @Transactional
    public DriverResponse register(CreateDriverRequest request) {

        String code = request.driverCode().trim();

        if (repository.existsByDriverCode(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Driver code already exists"
            );
        }

        DriverEntity driver = new DriverEntity(code);

        DriverEntity saved = repository.save(driver);

        return new DriverResponse(
                saved.getId(),
                saved.getDriverCode(),
                saved.getStatus(),
                saved.getAvailableAt()
        );
    }



    @Transactional
    public DriverResponse updateDriverStatus(
            Long driverId,
            DriverStatusRequest request
    ) {
        // Acquire the SAME coordination lock used by allocation.
        allocationCoordinationRepository.findByIdForUpdate((short) 1)
                .orElseThrow(() -> new IllegalStateException(
                        "Allocation coordination row is missing"
                ));

        DriverEntity driver = repository.findById(driverId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Driver not found with ID: " + driverId
                ));

        switch (request.status()) {
            case ACTIVE -> driver.activate();
            case INACTIVE -> driver.deactivate();
        }

        // Entity is managed by JPA; changes are persisted on flush/commit.
        return toResponse(driver);
    }

    @Transactional(readOnly = true)
    public Page<DriverResponse> getAllDrivers(
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_PAGINATION",
                    "Page must be >= 0 and size must be between 1 and 100"
            );
        }

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_SORT_FIELD",
                    "Unsupported sort field: " + sortBy
            );
        }

        Sort.Direction sortDirection;

        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_SORT_DIRECTION",
                    "Direction must be asc or desc"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        return repository.findAll(pageable)
                .map(this::toResponse);
    }


    @Transactional(readOnly = true)
    public DriverResponse getDriverById(Long id) {
        DriverEntity driver = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Driver not found with ID: " + id
                ));

        return toResponse(driver);
    }

    private DriverResponse toResponse(DriverEntity driver) {
        return new DriverResponse(
                driver.getId(),
                driver.getDriverCode(),
                driver.getStatus(),
                driver.getAvailableAt()
        );
    }
}
