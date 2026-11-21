
package com.loginext.challenge.driverAllocation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI driverAllocationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LogiNext Driver Allocation API")
                        .version("1.0.0")
                        .description("""
                                REST API for driver management and
                                deterministic order allocation.

                                Allocation rules:
                                - Select the lowest-numbered available
                                  ACTIVE driver.
                                - Driver availability is updated after
                                  successful allocation.
                                - Duplicate order references are
                                  handled idempotently.
                                - Chronological order processing is
                                  enforced.
                                - PostgreSQL transaction coordination
                                  protects concurrent allocations.
                                """));
    }
}
