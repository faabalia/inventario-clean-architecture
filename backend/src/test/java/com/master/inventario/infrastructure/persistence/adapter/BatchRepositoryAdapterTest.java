package com.master.inventario.infrastructure.persistence.adapter;

import com.master.inventario.domain.model.Batch;
import com.master.inventario.domain.model.Product;
import com.master.inventario.infrastructure.persistence.entity.BatchEntity;
import com.master.inventario.infrastructure.persistence.mapper.BatchMapper;
import com.master.inventario.infrastructure.persistence.repository.BatchJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BatchRepositoryAdapterTest {

    private BatchJpaRepository batchJpaRepository;
    private BatchMapper batchMapper;
    private BatchRepositoryAdapter adapter;

    // Shared fixtures
    private Product product;
    private Batch domainBatch;
    private BatchEntity batchEntity;

    @BeforeEach
    void setUp() {
        batchJpaRepository = mock(BatchJpaRepository.class);
        batchMapper = mock(BatchMapper.class);
        adapter = new BatchRepositoryAdapter(batchJpaRepository, batchMapper);

        product = new Product(1L, "SKU-1", "Milk", "Whole milk");
        domainBatch = new Batch(null, product, 10, LocalDate.now().plusDays(30), LocalDate.now());
        batchEntity = BatchEntity.builder().id(1L).quantity(10).build();
    }

    // --- save ---

    @Test
    @DisplayName("save: should map to entity, persist, and map back to domain")
    void save_shouldPersistAndReturnMappedDomain() {
        Batch savedDomain = new Batch(1L, product, 10, LocalDate.now().plusDays(30), LocalDate.now());

        when(batchMapper.toEntity(domainBatch)).thenReturn(batchEntity);
        when(batchJpaRepository.save(batchEntity)).thenReturn(batchEntity);
        when(batchMapper.toDomain(batchEntity)).thenReturn(savedDomain);

        Batch result = adapter.save(domainBatch);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(10, result.getQuantity());

        verify(batchMapper).toEntity(domainBatch);
        verify(batchJpaRepository).save(batchEntity);
        verify(batchMapper).toDomain(batchEntity);
    }

    // --- findById ---

    @Test
    @DisplayName("findById: should return mapped domain when batch exists")
    void findById_shouldReturnDomainWhenFound() {
        Batch expectedBatch = new Batch(1L, product, 10, LocalDate.now().plusDays(30), LocalDate.now());

        when(batchJpaRepository.findWithProductById(1L)).thenReturn(Optional.of(batchEntity));
        when(batchMapper.toDomain(batchEntity)).thenReturn(expectedBatch);

        Optional<Batch> result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(batchJpaRepository).findWithProductById(1L);
    }

    @Test
    @DisplayName("findById: should return empty Optional when batch does not exist")
    void findById_shouldReturnEmptyWhenNotFound() {
        when(batchJpaRepository.findWithProductById(99L)).thenReturn(Optional.empty());

        Optional<Batch> result = adapter.findById(99L);

        assertTrue(result.isEmpty());
        verify(batchMapper, never()).toDomain(any());
    }

    // --- findByProductId ---

    @Test
    @DisplayName("findByProductId: should return all batches mapped to domain")
    void findByProductId_shouldReturnMappedList() {
        BatchEntity entity2 = BatchEntity.builder().id(2L).quantity(5).build();
        Batch batch1 = new Batch(1L, product, 10, LocalDate.now().plusDays(30), LocalDate.now());
        Batch batch2 = new Batch(2L, product, 5, LocalDate.now().plusDays(15), LocalDate.now());

        when(batchJpaRepository.findByProductId(1L)).thenReturn(List.of(batchEntity, entity2));
        when(batchMapper.toDomain(batchEntity)).thenReturn(batch1);
        when(batchMapper.toDomain(entity2)).thenReturn(batch2);

        List<Batch> result = (List<Batch>) adapter.findByProductId(1L);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        verify(batchJpaRepository).findByProductId(1L);
        verify(batchMapper, times(2)).toDomain(any(BatchEntity.class));
    }

    @Test
    @DisplayName("findByProductId: should return empty list when product has no batches")
    void findByProductId_shouldReturnEmptyListWhenNoBatches() {
        when(batchJpaRepository.findByProductId(1L)).thenReturn(List.of());

        List<Batch> result = (List<Batch>) adapter.findByProductId(1L);

        assertTrue(result.isEmpty());
        verify(batchMapper, never()).toDomain(any());
    }

    // --- deleteById ---

    @Test
    @DisplayName("deleteById: should delegate directly to JPA repository")
    void deleteById_shouldDelegateToJpaRepository() {
        adapter.deleteById(1L);

        verify(batchJpaRepository).deleteById(1L);
        verifyNoInteractions(batchMapper);
    }
}

