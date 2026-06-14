package com.master.inventario.usecase;

import com.master.inventario.domain.exception.ProductValidationException;
import com.master.inventario.domain.model.Product;
import com.master.inventario.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateProductUseCaseTest {

    private ProductRepository productRepository;
    private CreateProductUseCase createProductUseCase;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        createProductUseCase = new CreateProductUseCase(productRepository);
    }

    @Test
    @DisplayName("Should throw when SKU is missing")
    void shouldThrowWhenSkuIsMissing() {
        Product product = new Product(null, "", "Milk", "Whole milk");

        ProductValidationException exception = assertThrows(
                ProductValidationException.class,
                () -> createProductUseCase.execute(product)
        );

        assertTrue(exception.getMessage().contains("SKU"));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when name is empty")
    void shouldThrowWhenNameIsEmpty() {
        Product product = new Product(null, "SKU-1", "   ", "Whole milk");

        ProductValidationException exception = assertThrows(
                ProductValidationException.class,
                () -> createProductUseCase.execute(product)
        );

        assertTrue(exception.getMessage().contains("Name"));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should save product when input is valid")
    void shouldSaveProductWhenInputIsValid() {
        Product input = new Product(null, "SKU-1", "Milk", "Whole milk");
        Product saved = new Product(1L, "SKU-1", "Milk", "Whole milk");

        when(productRepository.save(input)).thenReturn(saved);

        Product result = createProductUseCase.execute(input);

        assertEquals(1L, result.getId());
        assertEquals("SKU-1", result.getSku());
        verify(productRepository).save(input);
    }
}

