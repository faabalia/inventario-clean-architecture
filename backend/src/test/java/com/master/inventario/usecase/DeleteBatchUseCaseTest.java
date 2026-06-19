package com.master.inventario.usecase;

import com.master.inventario.domain.exception.BatchNotFoundException;
import com.master.inventario.domain.model.Batch;
import com.master.inventario.domain.model.Product;
import com.master.inventario.domain.repository.BatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteBatchUseCaseTest {

    private BatchRepository batchRepository;
    private DeleteBatchUseCase deleteBatchUseCase;

    @BeforeEach
    void setUp() {
        batchRepository = mock(BatchRepository.class);
        deleteBatchUseCase = new DeleteBatchUseCase(batchRepository);
    }

    @Test
    @DisplayName("Should delete batch when it exists")
    void shouldDeleteBatchWhenItExists() {
        Product product = new Product(1L, "SKU-1", "Milk", "Whole milk");
        Batch batch = new Batch(5L, product, 10, LocalDate.now().plusDays(10), LocalDate.now());

        when(batchRepository.findById(5L)).thenReturn(Optional.of(batch));

        deleteBatchUseCase.execute(5L);

        verify(batchRepository).deleteById(5L);
    }

    @Test
    @DisplayName("Should throw when batch does not exist")
    void shouldThrowWhenBatchDoesNotExist() {
        when(batchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BatchNotFoundException.class, () -> deleteBatchUseCase.execute(99L));

        verify(batchRepository, never()).deleteById(99L);
    }
}

