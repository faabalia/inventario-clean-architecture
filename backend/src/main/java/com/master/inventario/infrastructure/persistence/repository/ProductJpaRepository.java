package com.master.inventario.infrastructure.persistence.repository;

import com.master.inventario.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Interfaz Spring Data JPA para la entidad ProductEntity.
 * NO esta es la interfaz del dominio, es una interfaz de acceso a datos específica de infraestructura.
 * El dominio usa la interfaz ProductRepository, esta es una implementación técnica.
 */
@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    /**
     * Busca un producto por su SKU.
     * @param sku el código SKU del producto
     * @return Optional con el producto si existe
     */
    Optional<ProductEntity> findBySku(String sku);
}

