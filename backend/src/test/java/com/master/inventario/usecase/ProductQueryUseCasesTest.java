package com.master.inventario.usecase;

import com.master.inventario.domain.exception.ProductNotFoundException;
import com.master.inventario.domain.model.Batch;
import com.master.inventario.domain.model.Product;
import com.master.inventario.domain.repository.BatchRepository;
import com.master.inventario.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductQueryUseCasesTest {

    private ProductRepository productRepository;
    private BatchRepository batchRepository;
    private ListProductsUseCase listProductsUseCase;
    private GetProductByIdUseCase getProductByIdUseCase;
    private ListProductStockEntriesUseCase listProductStockEntriesUseCase;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        batchRepository = mock(BatchRepository.class);
        listProductsUseCase = new ListProductsUseCase(productRepository);
        getProductByIdUseCase = new GetProductByIdUseCase(productRepository);
        listProductStockEntriesUseCase = new ListProductStockEntriesUseCase(productRepository, batchRepository);
    }

    @Test
    @DisplayName("Should list products with pagination")
    void shouldListProductsWithPagination() {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Product> page = new PageImpl<>(List.of(
                new Product(1L, "SKU-1", "Milk", "Desc"),
                new Product(2L, "SKU-2", "Bread", "Desc")
        ), pageable, 3);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Product> result = listProductsUseCase.execute(pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
    }

    @Test
    @DisplayName("Should return product by id")
    void shouldReturnProductById() {
        Product product = new Product(1L, "SKU-1", "Milk", "Desc");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = getProductByIdUseCase.execute(1L);

        assertEquals("SKU-1", result.getSku());
        assertEquals("Milk", result.getName());
    }

    @Test
    @DisplayName("Should throw when product does not exist")
    void shouldThrowWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> getProductByIdUseCase.execute(99L));
    }

    @Test
    @DisplayName("Should list stock entries for a product")
    void shouldListStockEntriesForAProduct() {
        Product product = new Product(1L, "SKU-1", "Milk", "Desc");
        Batch batch1 = new Batch(10L, product, 5, LocalDate.now().plusDays(10), LocalDate.now());
        Batch batch2 = new Batch(11L, product, 7, LocalDate.now().plusDays(20), LocalDate.now());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(batchRepository.findByProductId(1L)).thenReturn(List.of(batch1, batch2));

        List<Batch> result = listProductStockEntriesUseCase.execute(1L);

        assertEquals(2, result.size());
        assertEquals(5, result.get(0).getQuantity());
    }
}

