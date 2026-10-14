package com.nexthire.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CorsConfig — Cross-Origin Resource Sharing (CORS) configuration.
 *
 * This allows the NextHire frontend (whether running locally or deployed on Vercel,
 * Render, Netlify, or custom domain) to communicate with this Spring Boot backend.
 */
@Configuration
public class CorsConfig {

    @Value("${nexthire.cors.allowed-origins:}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // ---------------------------------------------------------------
        // Allowed Origins — Local dev, Vercel, Render, Netlify, GitHub Pages
        // ---------------------------------------------------------------
        List<String> originPatterns = new ArrayList<>(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://*.vercel.app",
                "https://*.onrender.com",
                "https://*.netlify.app",
                "https://*.github.io",
                "*"
        ));

        // Append any environment-configured origins (comma-separated)
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(originPatterns::add);
        }

        config.setAllowedOriginPatterns(originPatterns);

        // Allow all standard HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allow all headers (including Authorization for JWT)
        config.setAllowedHeaders(List.of("*"));

        // Allow cookies / credentials
        config.setAllowCredentials(true);

        // Cache preflight response for 1 hour (reduces OPTIONS requests)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply CORS settings to ALL routes
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
