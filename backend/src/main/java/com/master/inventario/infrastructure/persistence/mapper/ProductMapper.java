package com.master.inventario.infrastructure.persistence.mapper;

import com.master.inventario.domain.model.Product;
import com.master.inventario.infrastructure.persistence.entity.ProductEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Product (dominio) y ProductEntity (infraestructura).
 * Aisla completamente el dominio de la tecnología de persistencia.
 */
@Component
public class ProductMapper {

    /**
     * Convierte una entidad JPA a un objeto de dominio.
     * @param entity la entidad JPA
     * @return el objeto de dominio
     */
    public Product toDomain(ProductEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Product(
                entity.getId(),
                entity.getSku(),
                entity.getName(),
                entity.getDescription()
        );
    }

    /**
     * Convierte un objeto de dominio a una entidad JPA.
     * @param domain el objeto de dominio
     * @return la entidad JPA
     */
    public ProductEntity toEntity(Product domain) {
        if (domain == null) {
            return null;
        }
        return ProductEntity.builder()
                .id(domain.getId())
                .sku(domain.getSku())
                .name(domain.getName())
                .description(domain.getDescription())
                .build();
    }
}

