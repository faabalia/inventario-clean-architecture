package com.master.inventario.usecase;

import com.master.inventario.domain.exception.ProductNotFoundException;
import com.master.inventario.domain.model.Product;
import com.master.inventario.domain.repository.ProductRepository;

public class GetProductByIdUseCase {

    private final ProductRepository productRepository;

    public GetProductByIdUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product execute(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}

