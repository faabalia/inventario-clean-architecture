package com.master.inventario.domain.repository;

import com.master.inventario.domain.model.Product;

import java.util.Optional;

/**
 * Contrato para el repositorio de Product.
 * Esta interfaz define el contrato que la infraestructura debe cumplir.
 */
public interface ProductRepository {

    /**
     * Guarda un producto en el repositorio.
     * @param product el producto a guardar
     * @return el producto guardado
     */
    Product save(Product product);

    /**
     * Busca un producto por su ID.
     * @param id el ID del producto
     * @return un Optional con el producto si existe
     */
    Optional<Product> findById(Long id);

    /**
     * Busca un producto por su SKU.
     * @param sku el SKU del producto
     * @return un Optional con el producto si existe
     */
    Optional<Product> findBySku(String sku);

    /**
     * Elimina un producto por su ID.
     * @param id el ID del producto a eliminar
     */
    void deleteById(Long id);
}

