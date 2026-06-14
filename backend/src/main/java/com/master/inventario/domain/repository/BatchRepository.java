package com.master.inventario.domain.repository;

import com.master.inventario.domain.model.Batch;

import java.util.Optional;

/**
 * Contrato para el repositorio de Batch.
 * Esta interfaz define el contrato que la infraestructura debe cumplir.
 * Pertenece al dominio porque es el dominio quien decide qué métodos necesita.
 */
public interface BatchRepository {

    /**
     * Guarda un lote en el repositorio.
     * @param batch el lote a guardar
     * @return el lote guardado
     */
    Batch save(Batch batch);

    /**
     * Busca un lote por su ID.
     * @param id el ID del lote
     * @return un Optional con el lote si existe
     */
    Optional<Batch> findById(Long id);

    /**
     * Busca todos los lotes de un producto.
     * @param productId el ID del producto
     * @return iterable de lotes
     */
    Iterable<Batch> findByProductId(Long productId);

    /**
     * Elimina un lote por su ID.
     * @param id el ID del lote a eliminar
     */
    void deleteById(Long id);
}

