
package com.loginext.challenge.driverAllocation.controller;

import com.loginext.challenge.driverAllocation.dto.CreateDriverRequest;
import com.loginext.challenge.driverAllocation.dto.DriverResponse;
import com.loginext.challenge.driverAllocation.dto.DriverStatusRequest;
import com.loginext.challenge.driverAllocation.service.DriverService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.web.PagedModel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Driver Management",
        description = "APIs to register, retrieve and manage drivers"
)
@RestController
@RequestMapping("/api/v1/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @Operation(
            summary = "Register a new driver",
            description = "Creates an ACTIVE driver with initial availability 0."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Driver created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Driver code already exists")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DriverResponse register(
            @Valid @RequestBody CreateDriverRequest request
    ) {
        return driverService.register(request);
    }







    @Operation(
            summary = "List drivers",
            description = "Returns a paginated list of registered drivers."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Drivers retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination")
    })
    @GetMapping
    public PagedModel<DriverResponse> getAllDrivers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return new PagedModel<>(
                driverService.getAllDrivers(page, size, sortBy, direction)
        );
    }

    @Operation(summary = "Get driver by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver found"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @GetMapping("/{id}")
    public DriverResponse getDriverById(@PathVariable Long id) {
        return driverService.getDriverById(id);
    }

    @Operation(
            summary = "Activate or deactivate driver",
            description = "Changes driver status using transaction coordination."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @PatchMapping("/{id}/status")
    public DriverResponse updateDriverStatus(
            @PathVariable Long id,
            @Valid @RequestBody DriverStatusRequest request
    ) {
        return driverService.updateDriverStatus(id, request);
    }
}
