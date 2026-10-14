package com.nexthire.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenApiConfig — Configures the Swagger/OpenAPI documentation.
 *
 * Access Swagger UI at: http://localhost:8080/swagger-ui.html
 * Access raw OpenAPI JSON at: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nexthireOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NextHire AI Platform — REST API")
                        .description("""
                                Backend REST API for the NextHire AI Interview Platform.
                                
                                This API provides endpoints for:
                                - User management (registration, login, profile)
                                - Interview management (create, start, submit, complete)
                                - Interview questions (browse by category, difficulty, type)
                                - Interview responses (save user answers)
                                - Interview results (scores, feedback, strengths, improvements)
                                - Dashboard statistics
                                - AI interviewer interface (mock - ready for future AI integration)
                                
                                NOTE: Currently using in-memory mock data. 
                                MySQL integration and JWT authentication will be added in the next phase.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NextHire Team")
                                .email("support@nexthire.ai"))
                        .license(new License()
                                .name("MIT License")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ));
    }
}
