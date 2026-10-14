package com.nexthire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * NextHireApplication — Main entry point for the NextHire Spring Boot backend.
 *
 * Run this class to start the server.
 * The server will start on http://localhost:8080
 *
 * Swagger UI: http://localhost:8080/swagger-ui.html
 */
@SpringBootApplication
public class NextHireApplication {

    public static void main(String[] args) {
        SpringApplication.run(NextHireApplication.class, args);
        System.out.println("""
                
                ╔══════════════════════════════════════════════════════════╗
                ║         NextHire AI Platform — Backend Running           ║
                ║  Server  : http://localhost:8080                         ║
                ║  Swagger : http://localhost:8080/swagger-ui.html         ║
                ║  API Docs: http://localhost:8080/v3/api-docs             ║
                ╚══════════════════════════════════════════════════════════╝
                """);
    }
}
