package com.master.inventario.usecase;

import com.master.inventario.domain.exception.InvalidBatchQuantityException;
import com.master.inventario.domain.exception.ExpiredBatchException;
import com.master.inventario.domain.model.Batch;
import com.master.inventario.domain.model.Product;
import com.master.inventario.domain.repository.BatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegisterStockEntryUseCaseTest {

    private RegisterStockEntryUseCase registerStockEntryUseCase;
    private BatchRepository batchRepositoryMock;

    @BeforeEach
    void setUp() {
        batchRepositoryMock = mock(BatchRepository.class);
        registerStockEntryUseCase = new RegisterStockEntryUseCase(batchRepositoryMock);
    }

    @Test
    @DisplayName("Debería lanzar excepción si la cantidad del lote es menor o igual a cero")
    void shouldThrowExceptionWhenQuantityIsLessThanOrEqualToZero() {
        // Arrange
        Product product = new Product(1L, "SKU-001", "Producto Test", "Descripción");
        Batch invalidBatch = new Batch(
                null,
                product,
                0,  // Cantidad inválida
                LocalDate.now().plusDays(10),
                LocalDate.now()
        );

        // Act & Assert
        InvalidBatchQuantityException exception = assertThrows(
                InvalidBatchQuantityException.class,
                () -> registerStockEntryUseCase.execute(invalidBatch),
                "Debería lanzar InvalidBatchQuantityException cuando la cantidad es 0"
        );

        assertTrue(exception.getMessage().contains("mayor a cero"));
        verify(batchRepositoryMock, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción si la cantidad del lote es negativa")
    void shouldThrowExceptionWhenQuantityIsNegative() {
        // Arrange
        Product product = new Product(1L, "SKU-002", "Producto Test", "Descripción");
        Batch invalidBatch = new Batch(
                null,
                product,
                -5,  // Cantidad negativa
                LocalDate.now().plusDays(10),
                LocalDate.now()
        );

        // Act & Assert
        assertThrows(
                InvalidBatchQuantityException.class,
                () -> registerStockEntryUseCase.execute(invalidBatch)
        );
        verify(batchRepositoryMock, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción si el lote ya está caducado")
    void shouldThrowExceptionWhenProductIsAlreadyExpired() {
        // Arrange
        Product product = new Product(1L, "SKU-003", "Producto Test", "Descripción");
        Batch expiredBatch = new Batch(
                null,
                product,
                100,
                LocalDate.now().minusDays(1),  // Caducado ayer
                LocalDate.now()
        );

        // Act & Assert
        ExpiredBatchException exception = assertThrows(
                ExpiredBatchException.class,
                () -> registerStockEntryUseCase.execute(expiredBatch),
                "Debería lanzar ExpiredBatchException cuando la fecha de caducidad es pasada"
        );

        assertTrue(exception.getMessage().contains("caducado"));
        verify(batchRepositoryMock, never()).save(any());
    }

    @Test
    @DisplayName("Debería guardar el lote cuando todas las validaciones pasan")
    void shouldSaveBatchWhenAllValidationsPass() {
        // Arrange
        Product product = new Product(1L, "SKU-004", "Producto Test", "Descripción");
        Batch validBatch = new Batch(
                null,
                product,
                50,
                LocalDate.now().plusDays(30),  // Válido
                LocalDate.now()
        );

        Batch savedBatch = new Batch(
                1L,
                product,
                50,
                LocalDate.now().plusDays(30),
                LocalDate.now()
        );

        when(batchRepositoryMock.save(any(Batch.class))).thenReturn(savedBatch);

        // Act
        Batch result = registerStockEntryUseCase.execute(validBatch);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(50, result.getQuantity());
        verify(batchRepositoryMock, times(1)).save(any(Batch.class));
    }

    @Test
    @DisplayName("Debería permitir un lote que caduca hoy")
    void shouldAllowBatchExpiringToday() {
        // Arrange
        Product product = new Product(1L, "SKU-005", "Producto Test", "Descripción");
        Batch batchExpiringToday = new Batch(
                null,
                product,
                100,
                LocalDate.now(),  // Caduca hoy (válido)
                LocalDate.now()
        );

        Batch savedBatch = new Batch(
                1L,
                product,
                100,
                LocalDate.now(),
                LocalDate.now()
        );

        when(batchRepositoryMock.save(any(Batch.class))).thenReturn(savedBatch);

        // Act
        Batch result = registerStockEntryUseCase.execute(batchExpiringToday);

        // Assert
        assertNotNull(result);
        verify(batchRepositoryMock, times(1)).save(any(Batch.class));
    }
}
