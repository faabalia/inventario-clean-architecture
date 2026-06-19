package com.master.inventario.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for product creation requests.
 *
 * Size constraints mirror the DB column limits (OWASP API3:2023 — Broken Object Property Level Authorization /
 * Unsafe Input Handling) to prevent DataIntegrityViolationException on oversized payloads.
 */
public record CreateProductRequest(
        @NotBlank(message = "sku is required")
        @Size(max = 50, message = "sku must not exceed 50 characters")
        String sku,

        @NotBlank(message = "name is required")
        @Size(max = 255, message = "name must not exceed 255 characters")
        String name,

        @Size(max = 500, message = "description must not exceed 500 characters")
        String description
) {
}

