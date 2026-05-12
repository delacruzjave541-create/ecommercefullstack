package com.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global CORS (Cross-Origin Resource Sharing) configuration.
 *
 * <p>Allows the frontend (served via Live Server on port 5500, or any local
 * port) to make HTTP requests to this Spring Boot backend (port 8080)
 * without the browser blocking the request.</p>
 *
 * <p>In production, replace the wildcard origin with the exact frontend
 * domain (e.g. {@code https://myshop.com}).</p>
 *
 * @author  Your Name
 * @version 1.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // Allow requests from Live Server (VS Code) and common dev ports
                .allowedOrigins(
                        "http://localhost:5500",
                        "http://127.0.0.1:5500",
                        "http://localhost:3000",
                        "http://localhost:4200"
                )
                // Allow all standard REST verbs
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // Allow these headers on incoming requests
                .allowedHeaders("Authorization", "Content-Type", "Accept")
                // Allow cookies / credentials if needed
                .allowCredentials(true)
                // Cache pre-flight OPTIONS response for 1 hour
                .maxAge(3600);
    }
}
