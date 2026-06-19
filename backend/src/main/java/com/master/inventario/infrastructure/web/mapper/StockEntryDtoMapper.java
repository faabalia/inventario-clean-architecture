package com.master.inventario.infrastructure.web.mapper;

import com.master.inventario.domain.model.Batch;
import com.master.inventario.domain.model.Product;
import com.master.inventario.infrastructure.web.dto.RegisterStockEntryRequest;
import com.master.inventario.infrastructure.web.dto.RegisterStockEntryResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class StockEntryDtoMapper {

    public Batch toDomain(RegisterStockEntryRequest request, Product product) {
        // receivedDate is always set by the server (OWASP API6:2023 — Mass Assignment mitigation).
        return new Batch(
                null,
                product,
                request.quantity(),
                request.expiryDate(),
                LocalDate.now()
        );
    }

    public RegisterStockEntryResponse toResponse(Batch batch) {
        return new RegisterStockEntryResponse(
                batch.getId(),
                batch.getProduct().getId(),
                batch.getQuantity(),
                batch.getExpiryDate(),
                batch.getReceivedDate()
        );
    }
}

