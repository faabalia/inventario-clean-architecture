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
        LocalDate receivedDate = request.receivedDate() != null ? request.receivedDate() : LocalDate.now();

        return new Batch(
                null,
                product,
                request.quantity(),
                request.expiryDate(),
                receivedDate
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

