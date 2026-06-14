package com.master.inventario.infrastructure.persistence.repository;

import com.master.inventario.infrastructure.persistence.entity.BatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interfaz Spring Data JPA para la entidad BatchEntity.
 * NO esta es la interfaz del dominio, es una interfaz de acceso a datos específica de infraestructura.
 * El dominio usa la interfaz BatchRepository, esta es una implementación técnica.
 */
@Repository
public interface BatchJpaRepository extends JpaRepository<BatchEntity, Long> {

    /**
     * Busca todos los lotes de un producto específico.
     * @param productId el ID del producto
     * @return iterable de lotes del producto
     */
    Iterable<BatchEntity> findByProductId(Long productId);
}

