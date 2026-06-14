package com.master.inventario.usecase;

import com.master.inventario.domain.model.Batch;
import com.master.inventario.domain.exception.InvalidBatchQuantityException;
import com.master.inventario.domain.exception.ExpiredBatchException;
import com.master.inventario.domain.repository.BatchRepository;

/**
 * Caso de uso: Registrar entrada de stock.
 * Orquesta la lógica de negocio para registrar un nuevo lote de producto.
 *
 * Responsabilidades:
 * - Validar que la cantidad sea válida (> 0)
 * - Validar que la fecha de caducidad no sea pasada
 * - Guardar el lote en el repositorio
 */
public class RegisterStockEntryUseCase {

    private final BatchRepository batchRepository;

    public RegisterStockEntryUseCase(BatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    /**
     * Ejecuta el caso de uso de registro de entrada de stock.
     *
     * @param batch el lote a registrar
     * @return el lote guardado
     * @throws InvalidBatchQuantityException si la cantidad es inválida
     * @throws ExpiredBatchException si el lote está caducado
     */
    public Batch execute(Batch batch) {
        validateBatch(batch);
        return batchRepository.save(batch);
    }

    /**
     * Valida todas las reglas de negocio del lote.
     *
     * @param batch el lote a validar
     * @throws InvalidBatchQuantityException si la cantidad es inválida (≤ 0)
     * @throws ExpiredBatchException si la fecha de caducidad es pasada
     */
    private void validateBatch(Batch batch) {
        if (!batch.isValidQuantity()) {
            throw new InvalidBatchQuantityException(
                    String.format(
                            "La cantidad del lote debe ser mayor a cero. Cantidad recibida: %d",
                            batch.getQuantity()
                    )
            );
        }

        if (!batch.isNotExpired()) {
            throw new ExpiredBatchException(
                    String.format(
                            "El lote ya está caducado. Fecha de caducidad: %s",
                            batch.getExpiryDate()
                    )
            );
        }
    }
}

