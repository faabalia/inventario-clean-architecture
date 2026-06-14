package com.master.inventario.infrastructure.persistence.adapter;

import com.master.inventario.domain.model.Batch;
import com.master.inventario.domain.repository.BatchRepository;
import com.master.inventario.infrastructure.persistence.mapper.BatchMapper;
import com.master.inventario.infrastructure.persistence.repository.BatchJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Adaptador que implementa la interfaz BatchRepository del dominio.
 * Convierte la interfaz técnica de Spring Data JPA en una interfaz de dominio pura.
 *
 * Este es un ejemplo de patrón Adapter: el dominio NO conoce sobre Spring Data,
 * JPA o la base de datos. Solo sabe que existe un BatchRepository que persiste Batches.
 */
@Component
public class BatchRepositoryAdapter implements BatchRepository {

    private final BatchJpaRepository batchJpaRepository;
    private final BatchMapper batchMapper;

    public BatchRepositoryAdapter(BatchJpaRepository batchJpaRepository, BatchMapper batchMapper) {
        this.batchJpaRepository = batchJpaRepository;
        this.batchMapper = batchMapper;
    }

    @Override
    public Batch save(Batch batch) {
        var entity = batchMapper.toEntity(batch);
        var savedEntity = batchJpaRepository.save(entity);
        return batchMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Batch> findById(Long id) {
        return batchJpaRepository.findById(id)
                .map(batchMapper::toDomain);
    }

    @Override
    public Iterable<Batch> findByProductId(Long productId) {
        return StreamSupport
                .stream(batchJpaRepository.findByProductId(productId).spliterator(), false)
                .map(batchMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        batchJpaRepository.deleteById(id);
    }
}

