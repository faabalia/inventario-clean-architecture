package com.master.inventario.usecase;

import com.master.inventario.domain.exception.ProductNotFoundException;
import com.master.inventario.domain.exception.ProductValidationException;
import com.master.inventario.domain.model.Product;
import com.master.inventario.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateProductUseCaseTest {

    private ProductRepository productRepository;
    private UpdateProductUseCase updateProductUseCase;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        updateProductUseCase = new UpdateProductUseCase(productRepository);
    }

    @Test
    @DisplayName("Should update product when payload is valid")
    void shouldUpdateProductWhenPayloadIsValid() {
        Product existing = new Product(1L, "SKU-1", "Old", "Old desc", 5);
        Product saved = new Product(1L, "SKU-1", "New", "New desc", 15);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        Product result = updateProductUseCase.execute(1L, "New", "New desc", 15);

        assertEquals("SKU-1", result.getSku());
        assertEquals("New", result.getName());
        assertEquals(15, result.getMinStock());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw when product does not exist")
    void shouldThrowWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> updateProductUseCase.execute(99L, "Milk", "Whole milk", 1));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw when min stock is negative")
    void shouldThrowWhenMinStockIsNegative() {
        assertThrows(ProductValidationException.class,
                () -> updateProductUseCase.execute(1L, "Milk", "Whole milk", -1));

        verify(productRepository, never()).findById(any());
        verify(productRepository, never()).save(any(Product.class));
    }
}

