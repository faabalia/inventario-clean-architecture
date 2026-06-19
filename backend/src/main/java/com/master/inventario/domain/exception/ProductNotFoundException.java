package com.master.inventario.domain.exception;

public class ProductNotFoundException extends DomainException {

    public ProductNotFoundException(Long productId) {
        super("Product not found with id: " + productId);
    }
}

