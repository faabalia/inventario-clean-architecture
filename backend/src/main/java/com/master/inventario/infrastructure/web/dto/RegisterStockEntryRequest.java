package com.master.inventario.infrastructure.web.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record RegisterStockEntryRequest(
        @NotNull(message = "productId is required")
        Long productId,

        @NotNull(message = "quantity is required")
        @Positive(message = "quantity must be greater than zero")
        Integer quantity,

        @NotNull(message = "expiryDate is required")
        @FutureOrPresent(message = "expiryDate cannot be in the past")
        LocalDate expiryDate,

        LocalDate receivedDate
) {
}

