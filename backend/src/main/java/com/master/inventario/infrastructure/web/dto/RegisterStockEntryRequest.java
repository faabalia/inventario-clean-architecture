package com.master.inventario.infrastructure.web.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * DTO for stock entry registration.
 *
 * receivedDate is intentionally absent (OWASP API6:2023 — Unrestricted Access to Sensitive Business Flows /
 * Mass Assignment). The reception date is always assigned by the server via LocalDate.now() to prevent
 * clients from backdating or manipulating stock records.
 */
public record RegisterStockEntryRequest(
        @NotNull(message = "productId is required")
        Long productId,

        @NotNull(message = "quantity is required")
        @Positive(message = "quantity must be greater than zero")
        Integer quantity,

        @NotNull(message = "expiryDate is required")
        @FutureOrPresent(message = "expiryDate cannot be in the past")
        LocalDate expiryDate
) {
}

