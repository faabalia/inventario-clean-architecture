package com.master.inventario.usecase;

import com.master.inventario.domain.exception.ProductValidationException;
import com.master.inventario.domain.model.Product;
import com.master.inventario.domain.repository.ProductRepository;

public class CreateProductUseCase {

    private final ProductRepository productRepository;

    public CreateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product execute(Product product) {
        validate(product);
        return productRepository.save(product);
    }

    private void validate(Product product) {
        if (product == null) {
            throw new ProductValidationException("Product is required");
        }
        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            throw new ProductValidationException("SKU is required");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new ProductValidationException("Name must not be empty");
        }
    }
}

