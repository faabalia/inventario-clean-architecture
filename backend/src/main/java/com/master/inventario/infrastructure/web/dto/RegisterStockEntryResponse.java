package com.master.inventario.infrastructure.web.dto;

import java.time.LocalDate;

public record RegisterStockEntryResponse(
        Long id,
        Long productId,
        Integer quantity,
        LocalDate expiryDate,
        LocalDate receivedDate
) {
}

