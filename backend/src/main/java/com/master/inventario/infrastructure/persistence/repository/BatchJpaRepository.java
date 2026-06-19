package com.master.inventario.infrastructure.persistence.repository;

import com.master.inventario.infrastructure.persistence.entity.BatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
    @Query("select b from BatchEntity b join fetch b.product where b.product.id = :productId")
    Iterable<BatchEntity> findByProductId(Long productId);

    @Query("select b from BatchEntity b join fetch b.product where b.id = :id")
    Optional<BatchEntity> findWithProductById(Long id);
}

