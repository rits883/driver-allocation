
package com.loginext.challenge.driverAllocation.controller;

import com.loginext.challenge.driverAllocation.dto.CreateOrderRequest;
import com.loginext.challenge.driverAllocation.dto.OrderAllocationResponse;
import com.loginext.challenge.driverAllocation.dto.OrderResponse;
import com.loginext.challenge.driverAllocation.service.OrderQueryService;
import com.loginext.challenge.driverAllocation.service.PersistentAllocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final PersistentAllocationService service;
    private final OrderQueryService orderQueryService;
    public OrderController(PersistentAllocationService service,OrderQueryService orderQueryService) {
        this.service = service;
        this.orderQueryService=orderQueryService;
    }

    @PostMapping
    public ResponseEntity<OrderAllocationResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        return ResponseEntity.ok(service.allocate(request));
    }

    @Operation(
            summary = "Get all orders",
            description = "Returns paginated orders with allocation information"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination or sorting parameters"
            )
    })
    @GetMapping
    public PagedModel<OrderResponse> getAllOrders(

            @Parameter(description = "Page number starting from zero")
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(description = "Number of records per page (1-100)")
            @RequestParam(defaultValue = "20")
            int size,

            @Parameter(description = "Field used for sorting")
            @RequestParam(defaultValue = "id")
            String sortBy,

            @Parameter(description = "Sort direction: asc or desc")
            @RequestParam(defaultValue = "desc")
            String direction
    ) {

        return new PagedModel<>(
                orderQueryService.getAllOrders(
                        page,
                        size,
                        sortBy,
                        direction
                )
        );
    }
}
