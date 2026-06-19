package com.master.inventario.usecase;

import com.master.inventario.domain.model.Product;
import com.master.inventario.domain.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ListProductsUseCase {

    private final ProductRepository productRepository;

    public ListProductsUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<Product> execute(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
}

