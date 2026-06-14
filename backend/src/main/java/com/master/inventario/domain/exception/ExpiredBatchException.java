package com.master.inventario.domain.exception;

/**
 * Excepción de dominio: Producto caducado.
 * Se lanza cuando el lote tiene una fecha de caducidad en el pasado.
 */
public class ExpiredBatchException extends DomainException {

    public ExpiredBatchException(String message) {
        super(message);
    }

    public ExpiredBatchException(String message, Throwable cause) {
        super(message, cause);
    }
}

