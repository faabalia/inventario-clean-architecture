package com.master.inventario.infrastructure.persistence.adapter;

import com.master.inventario.domain.model.Product;
import com.master.inventario.domain.repository.ProductRepository;
import com.master.inventario.infrastructure.persistence.mapper.ProductMapper;
import com.master.inventario.infrastructure.persistence.repository.ProductJpaRepository;
import org.springframework.stereotype.Component;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

/**
 * Adaptador que implementa la interfaz ProductRepository del dominio.
 * Convierte la interfaz técnica de Spring Data JPA en una interfaz de dominio pura.
 *
 * Este es un ejemplo de patrón Adapter: el dominio NO conoce sobre Spring Data,
 * JPA o la base de datos. Solo sabe que existe un ProductRepository que persiste Products.
 */
@Component
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final ProductMapper productMapper;

    public ProductRepositoryAdapter(ProductJpaRepository productJpaRepository, ProductMapper productMapper) {
        this.productJpaRepository = productJpaRepository;
        this.productMapper = productMapper;
    }

    @Override
    public Product save(Product product) {
        var entity = productMapper.toEntity(product);
        var savedEntity = productJpaRepository.save(entity);
        return productMapper.toDomain(savedEntity);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productJpaRepository.findAll(pageable).map(productMapper::toDomain);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id)
                .map(productMapper::toDomain);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return productJpaRepository.findBySku(sku)
                .map(productMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        productJpaRepository.deleteById(id);
    }
}

