package com.master.inventario.infrastructure.web.controller;

import com.master.inventario.infrastructure.web.dto.CreateProductRequest;
import com.master.inventario.infrastructure.web.dto.ProductResponse;
import com.master.inventario.infrastructure.web.mapper.ProductDtoMapper;
import com.master.inventario.usecase.CreateProductUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final ProductDtoMapper productDtoMapper;

    public ProductController(CreateProductUseCase createProductUseCase, ProductDtoMapper productDtoMapper) {
        this.createProductUseCase = createProductUseCase;
        this.productDtoMapper = productDtoMapper;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        var toCreate = productDtoMapper.toDomain(request);
        var saved = createProductUseCase.execute(toCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(productDtoMapper.toResponse(saved));
    }
}

