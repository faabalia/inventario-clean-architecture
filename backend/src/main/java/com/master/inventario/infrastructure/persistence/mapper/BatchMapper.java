package com.master.inventario.infrastructure.persistence.mapper;

import com.master.inventario.domain.model.Batch;
import com.master.inventario.domain.model.Product;
import com.master.inventario.infrastructure.persistence.entity.BatchEntity;
import com.master.inventario.infrastructure.persistence.entity.ProductEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Batch (dominio) y BatchEntity (infraestructura).
 * Aisla completamente el dominio de la tecnología de persistencia.
 */
@Component
public class BatchMapper {

    private final ProductMapper productMapper;

    public BatchMapper(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    /**
     * Convierte una entidad JPA a un objeto de dominio.
     * @param entity la entidad JPA
     * @return el objeto de dominio
     */
    public Batch toDomain(BatchEntity entity) {
        if (entity == null) {
            return null;
        }

        Product product = productMapper.toDomain(entity.getProduct());

        return new Batch(
                entity.getId(),
                product,
                entity.getQuantity(),
                entity.getExpiryDate(),
                entity.getReceivedDate()
        );
    }

    /**
     * Convierte un objeto de dominio a una entidad JPA.
     * @param domain el objeto de dominio
     * @return la entidad JPA
     */
    public BatchEntity toEntity(Batch domain) {
        if (domain == null) {
            return null;
        }

        ProductEntity productEntity = productMapper.toEntity(domain.getProduct());

        return BatchEntity.builder()
                .id(domain.getId())
                .product(productEntity)
                .quantity(domain.getQuantity())
                .expiryDate(domain.getExpiryDate())
                .receivedDate(domain.getReceivedDate())
                .build();
    }
}

