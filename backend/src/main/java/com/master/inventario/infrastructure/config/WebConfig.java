package com.master.inventario.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Web layer configuration.
 *
 * Pageable hardening (OWASP API4:2023 — Unrestricted Resource Consumption):
 * - Default page size: 10
 * - Maximum page size: 100 (requests with size > 100 are silently capped)
 * This prevents malicious requests like ?size=100000 from exhausting memory or DB resources.
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig implements WebMvcConfigurer {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String[] allowedOrigins;

    @Bean
    PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        return resolver -> {
            resolver.setFallbackPageable(org.springframework.data.domain.PageRequest.of(0, DEFAULT_PAGE_SIZE));
            resolver.setMaxPageSize(MAX_PAGE_SIZE);
        };
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsRegistration cors = registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization")
                .allowCredentials(true)
                .maxAge(3600);

        cors.exposedHeaders("Location");
    }

    @Bean
    public OncePerRequestFilter securityHeadersFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setHeader("X-Frame-Options", "DENY");
                response.setHeader("Referrer-Policy", "no-referrer");
                response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
                response.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'; base-uri 'none'");
                filterChain.doFilter(request, response);
            }
        };
    }
}

