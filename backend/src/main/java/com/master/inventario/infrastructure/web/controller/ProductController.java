package com.master.inventario.infrastructure.web.controller;

import com.master.inventario.infrastructure.web.dto.RegisterStockEntryResponse;
import com.master.inventario.infrastructure.web.dto.CreateProductRequest;
import com.master.inventario.infrastructure.web.dto.ProductResponse;
import com.master.inventario.infrastructure.web.dto.UpdateProductRequest;
import com.master.inventario.infrastructure.web.mapper.ProductDtoMapper;
import com.master.inventario.infrastructure.web.mapper.StockEntryDtoMapper;
import com.master.inventario.usecase.GetProductByIdUseCase;
import com.master.inventario.usecase.ListProductStockEntriesUseCase;
import com.master.inventario.usecase.ListProductsUseCase;
import com.master.inventario.usecase.CreateProductUseCase;
import com.master.inventario.usecase.UpdateProductUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;
    private final ListProductStockEntriesUseCase listProductStockEntriesUseCase;
    private final ProductDtoMapper productDtoMapper;
    private final StockEntryDtoMapper stockEntryDtoMapper;


    public ProductController(
            CreateProductUseCase createProductUseCase,
            UpdateProductUseCase updateProductUseCase,
            ListProductsUseCase listProductsUseCase,
            GetProductByIdUseCase getProductByIdUseCase,
            ListProductStockEntriesUseCase listProductStockEntriesUseCase,
            ProductDtoMapper productDtoMapper,
            StockEntryDtoMapper stockEntryDtoMapper
    ) {
        this.createProductUseCase = createProductUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.listProductsUseCase = listProductsUseCase;
        this.getProductByIdUseCase = getProductByIdUseCase;
        this.listProductStockEntriesUseCase = listProductStockEntriesUseCase;
        this.productDtoMapper = productDtoMapper;
        this.stockEntryDtoMapper = stockEntryDtoMapper;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        var toCreate = productDtoMapper.toDomain(request);
        var saved = createProductUseCase.execute(toCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(productDtoMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        var updated = updateProductUseCase.execute(id, request.name(), request.description(), request.minStock());
        return ResponseEntity.ok(productDtoMapper.toResponse(updated));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> list(Pageable pageable) {
        Page<ProductResponse> response = listProductsUseCase.execute(pageable).map(productDtoMapper::toResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productDtoMapper.toResponse(getProductByIdUseCase.execute(id)));
    }

    @GetMapping("/{id}/stock-entries")
    public ResponseEntity<List<RegisterStockEntryResponse>> getStockEntries(@PathVariable Long id) {
        List<RegisterStockEntryResponse> response = listProductStockEntriesUseCase.execute(id).stream()
                .map(stockEntryDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}

