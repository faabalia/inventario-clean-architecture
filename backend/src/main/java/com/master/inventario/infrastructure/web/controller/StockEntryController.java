package com.master.inventario.infrastructure.web.controller;

import com.master.inventario.domain.model.Product;
import com.master.inventario.infrastructure.web.dto.RegisterStockEntryRequest;
import com.master.inventario.infrastructure.web.dto.RegisterStockEntryResponse;
import com.master.inventario.infrastructure.web.mapper.StockEntryDtoMapper;
import com.master.inventario.usecase.DeleteBatchUseCase;
import com.master.inventario.usecase.GetProductByIdUseCase;
import com.master.inventario.usecase.RegisterStockEntryUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-entries")
public class StockEntryController {

    private final RegisterStockEntryUseCase registerStockEntryUseCase;
    private final DeleteBatchUseCase deleteBatchUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;
    private final StockEntryDtoMapper stockEntryDtoMapper;

    public StockEntryController(
            RegisterStockEntryUseCase registerStockEntryUseCase,
            DeleteBatchUseCase deleteBatchUseCase,
            GetProductByIdUseCase getProductByIdUseCase,
            StockEntryDtoMapper stockEntryDtoMapper
    ) {
        this.registerStockEntryUseCase = registerStockEntryUseCase;
        this.deleteBatchUseCase = deleteBatchUseCase;
        this.getProductByIdUseCase = getProductByIdUseCase;
        this.stockEntryDtoMapper = stockEntryDtoMapper;
    }

    @PostMapping
    public ResponseEntity<RegisterStockEntryResponse> create(@Valid @RequestBody RegisterStockEntryRequest request) {
        Product product = getProductByIdUseCase.execute(request.productId());

        var batchToSave = stockEntryDtoMapper.toDomain(request, product);
        var savedBatch = registerStockEntryUseCase.execute(batchToSave);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockEntryDtoMapper.toResponse(savedBatch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteBatchUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}

