package com.loginext.challenge.driverAllocation.dto;

import java.util.List;
public record AllocationResponse( int totalOrders,
                                  int assignedOrders,
                                  int rejectedOrders,
                                  List<Integer> assignments) {
}
