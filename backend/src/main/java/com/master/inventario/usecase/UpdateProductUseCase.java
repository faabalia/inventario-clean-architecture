package com.master.inventario.usecase;

import com.master.inventario.domain.exception.ProductNotFoundException;
import com.master.inventario.domain.exception.ProductValidationException;
import com.master.inventario.domain.model.Product;
import com.master.inventario.domain.repository.ProductRepository;

public class UpdateProductUseCase {

    private final ProductRepository productRepository;

    public UpdateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product execute(Long productId, String name, String description, Integer minStock) {
        validate(name, minStock);

        Product existing = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Product updated = new Product(
                existing.getId(),
                existing.getSku(),
                name,
                description,
                minStock
        );

        return productRepository.save(updated);
    }

    private void validate(String name, Integer minStock) {
        if (name == null || name.trim().isEmpty()) {
            throw new ProductValidationException("Name must not be empty");
        }
        if (minStock == null) {
            throw new ProductValidationException("Min stock is required");
        }
        if (minStock < 0) {
            throw new ProductValidationException("Min stock must be greater than or equal to zero");
        }
    }
}

