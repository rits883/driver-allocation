
package com.loginext.challenge.driverAllocation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.loginext.challenge.driverAllocation.dto.AllocationResponse;
import com.loginext.challenge.driverAllocation.exception.GlobalExceptionHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AllocationControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {


        objectMapper = new ObjectMapper();



    }



    @Test
    void shouldRejectInvalidDriverCount() throws Exception {

        String request = """
                {
                  "driverCount": 0,
                  "orders": [
                    {"orderTime": 1, "travelTime": 10}
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/allocations/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Validation failed"));
    }

    @Test
    void shouldRejectEmptyOrders() throws Exception {

        String request = """
                {
                  "driverCount": 2,
                  "orders": []
                }
                """;

        mockMvc.perform(post("/api/v1/allocations/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectNegativeOrderTime() throws Exception {

        String request = """
                {
                  "driverCount": 2,
                  "orders": [
                    {"orderTime": -1, "travelTime": 10}
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/allocations/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMissingTravelTime() throws Exception {

        String request = """
                {
                  "driverCount": 2,
                  "orders": [
                    {"orderTime": 1}
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/allocations/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }
}
