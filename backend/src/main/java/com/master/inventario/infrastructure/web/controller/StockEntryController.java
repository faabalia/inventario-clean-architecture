package com.master.inventario.infrastructure.web.controller;

import com.master.inventario.domain.model.Product;
import com.master.inventario.domain.repository.ProductRepository;
import com.master.inventario.infrastructure.web.dto.RegisterStockEntryRequest;
import com.master.inventario.infrastructure.web.dto.RegisterStockEntryResponse;
import com.master.inventario.infrastructure.web.mapper.StockEntryDtoMapper;
import com.master.inventario.usecase.RegisterStockEntryUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/stock-entries")
public class StockEntryController {

    private final RegisterStockEntryUseCase registerStockEntryUseCase;
    private final ProductRepository productRepository;
    private final StockEntryDtoMapper stockEntryDtoMapper;

    public StockEntryController(
            RegisterStockEntryUseCase registerStockEntryUseCase,
            ProductRepository productRepository,
            StockEntryDtoMapper stockEntryDtoMapper
    ) {
        this.registerStockEntryUseCase = registerStockEntryUseCase;
        this.productRepository = productRepository;
        this.stockEntryDtoMapper = stockEntryDtoMapper;
    }

    @PostMapping
    public ResponseEntity<RegisterStockEntryResponse> create(@Valid @RequestBody RegisterStockEntryRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        var batchToSave = stockEntryDtoMapper.toDomain(request, product);
        var savedBatch = registerStockEntryUseCase.execute(batchToSave);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockEntryDtoMapper.toResponse(savedBatch));
    }
}

